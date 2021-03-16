package io.freethejazz.paintmix.domain

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable

@PlanningEntity
data class PaintVat(
    var volume: Float? = null,
    var desiredBlueShade: BlueShade? = null,

    @InverseRelationShadowVariable(sourceVariableName = "paintVat")
    var assignedBuckets: MutableList<PaintBucket>? = mutableListOf()
)
