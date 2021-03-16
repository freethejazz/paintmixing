package io.freethejazz.paintmix.domain.rest

import io.freethejazz.paintmix.domain.PaintMixSolution
import org.optaplanner.core.api.solver.SolverJob
import org.optaplanner.core.api.solver.SolverManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.ExecutionException

@RestController
@RequestMapping("/paintmix")
class PaintMixController (
    val solverManager: SolverManager<PaintMixSolution, UUID>
        ) {

    @PostMapping("/solve")
    fun solve(@RequestBody problem: PaintMixSolution) : PaintMixSolution {
        val problemId: UUID = UUID.randomUUID();
        val solverJob: SolverJob<PaintMixSolution, UUID> = solverManager.solve(problemId, problem);
        val solution: PaintMixSolution;
        try {
            solution = solverJob.finalBestSolution;
        } catch (e: ExecutionException) {
            throw e
            // throw IllegalStateException()
        }
        return solution
    }
}