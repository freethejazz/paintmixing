package io.freethejazz.paintmix.domain

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore

@PlanningSolution
data class PaintMixSolution(
    @PlanningEntityCollectionProperty
    val paintBuckets: List<PaintBucket>,

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "paintVats")
    val paintVats: List<PaintVat>,

    @PlanningScore
    var score: HardMediumSoftScore? = null
) {
    constructor() : this(emptyList(), emptyList(), null)
}
