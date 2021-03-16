# Paintmixing with Optaplanner

A generic prototype for mixing paint (a variation of bin packing) using
optaplanner.

![image](https://user-images.githubusercontent.com/1261421/111267520-220ac880-8680-11eb-9946-3d9ea8f77ddc.png)

The main branch has the minimal solution built, which optimizes the
problem of mixing buckets of blue paint into a vat. The problem is
overconstrined and it is possible (and desired) for a paint bucket to be
unallocated to a vat rather than overflowing the vat.

The rules in the main branch try to allocate as many buckets to a vat as
possible without overflowing it.

Once the server is up, you can trigger an example payload with
`./post-request.sh`. I haven't yet set up testing classes around the POC
so the post request is the quickest way of showing usage/data examples.

## Current blocker

The desired solution will also optimize for a particular desired shade
of blue, which can be calculated using a weighted average of the
buckets.

The `BlueShade` is named shade of blue, which is just a blend of blue
paint and white paint. The percent blue is the percent of blue paint
that went into the shade:

 - All blue paint and no white paint would be 100
 - No blue paint and all white paint would be 0

The resulting mix of shades of blue will result in a new shade of blue.
That shade can be determined using a wieghted average calculation on
paint buckets used. The percent blue is weighted by the volume:

```
sum(percentBlue of buckets * quantity of buckets) / sum(quantity of buckets) = percentBlue of vat
```

This weighted average calculation is my current blocker. The options
I've considered are:

 - Add a shadow variable to the vat and a function in the PaintVat class
   that calculates the weighted average.
 - Calculate the weighted average using accumulators in a tricky way.
 - Write a custom accumulator for better reusability.

### Shadow variable + convenience method
My original solution was to add an `InverseRelationShadoVariable` on
the `PaintVat` class and add a convenience method to calculate things
like weighted average of the buckets assigned to the vat. I know this
means I don't have the advantage of incremental calculation, but for the
purposes of this proof of concept the performance hit is not critical.

1. Add `@PlanningEntity` to the `PaintVat` class
1. Add a mutable list member to the `PaintVat` class with the inverse relation annotation.
```
  //...
  @InverseRelationShadowVariable(sourceVariableName = "paintVat")
  var assignedBuckets: MutableList<PaintBucket>? = mutableListOf()
  //...
```

Without proceeding to writing the convenience method, this already fails
due to a StackOverflow after the initialization score is reported:

```
2021-03-16 15:23:57.891  INFO 20240 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2021-03-16 15:23:58.548  INFO 20240 --- [pool-1-thread-1] o.o.core.impl.solver.DefaultSolver       : Solving started: time spent (151), best score (0hard/-400medium/0soft), environment mode (REPRODUCIBLE), move thread count (NONE), random (JDK with seed 0).
2021-03-16 15:23:58.586 ERROR 20240 --- [nio-8080-exec-2] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException: java.lang.StackOverflowError] with root cause

java.lang.StackOverflowError: null
  at io.freethejazz.paintmix.domain.BlueShade.hashCode(BlueShade.kt) ~[main/:na]
  at io.freethejazz.paintmix.domain.PaintBucket.hashCode(PaintBucket.kt) ~[main/:na]
  at java.base/java.util.ArrayList.hashCodeRange(ArrayList.java:627) ~[na:na]
  at java.base/java.util.ArrayList.hashCode(ArrayList.java:614) ~[na:na]
  at io.freethejazz.paintmix.domain.PaintVat.hashCode(PaintVat.kt) ~[main/:na]
  at io.freethejazz.paintmix.domain.PaintBucket.hashCode(PaintBucket.kt) ~[main/:na]
  at java.base/java.util.ArrayList.hashCodeRange(ArrayList.java:627) ~[na:na]
  at java.base/java.util.ArrayList.hashCode(ArrayList.java:614) ~[na:na]
  at io.freethejazz.paintmix.domain.PaintVat.hashCode(PaintVat.kt) ~[main/:na]
  at io.freethejazz.paintmix.domain.PaintBucket.hashCode(PaintBucket.kt) ~[main/:na]
  at java.base/java.util.ArrayList.hashCodeRange(ArrayList.java:627) ~[na:na]
  at java.base/java.util.ArrayList.hashCode(ArrayList.java:614) ~[na:na]
  at io.freethejazz.paintmix.domain.PaintVat.hashCode(PaintVat.kt) ~[main/:na]
  at io.freethejazz.paintmix.domain.PaintBucket.hashCode(PaintBucket.kt) ~[main/:na]
  ...
  ...
  ...
```

I first thought this was an issue with the circular dependency at
serialization time of the response, but I don't think it's the case and
I tested with appropriate Jackson annotations to ignore the fields and
mark it as a bidirectional relationship but the code clearly fails
before Jackson gets to it.

The branch `shadow-var` includes this change and can be used to verify
the failure.


### Pure accumulator approach
I am able to the weighted numerator and the denominator for the weighted
average calculation, but can't divide them in the condition:

```
rule "Ensure an appropriate shade of blue"
    when
        $vat : PaintVat( $desiredBluePercent :
desiredBlueShad.percentBlue )
        exists PaintBucket( paintVat == $vat )
        accumulate(
            $r : PaintBucket( paintVat == $vat );
            $weightedNumer : sum($r.quantity.doubleValue() * $r.blueShade.ppercentBlue.doubleValue()),
            $denom : sum($r.quantity.doubleValue() * 1.0);
            // This fails due to casting issues between a
            // java.lang.Object and a java.lang.Float.
            //I've tried w/o eval, w/ .doubleValue(), etc...
            eval ( $weightedNumer / $denom ) < 75
        )
    then
        // If I remove the condition on the accumulator this prints out
as expected
        System.out.println("resulting blue percent: " + $weightedNumer / $denom);
end
```

### Custom Accumulator
My ideal custom accumulator would feel like this:

```
rule "Ensure an appropriate shade of blue"
    when
        $vat : PaintVat( $desiredBluePercent :
desiredBlueShad.percentBlue )
        exists PaintBucket( paintVat == $vat )
        accumulate(
            $r : PaintBucket( paintVat == $vat );
            $wtd_avg_blue : weightedAverage($r.blueShade.percentBlue, $r.quantity.doubleValue());
            $wtd_avg_blue > $desiredBluePercent + 1,
            $wtd_avg_blue < $desiredBluePercent - 1
        )
    then
      // Shade of blue is out of an acceptable tolerance, penalize solution
end
```
