package io.freethejazz.paintmix.domain

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity
data class PaintBucket(
    var blueShade: BlueShade? = null,
    var volume: Float? = null,

    @PlanningVariable(valueRangeProviderRefs = ["paintVats"], nullable = true)
    var paintVat: PaintVat? = null,
)
