package org.processmining.models.time_driven_behavior

import java.util.concurrent.TimeUnit

/**
 * @author Ivan Shugurov
 * Created on 03.03.2014
 */

data class NoiseEvent @JvmOverloads constructor(
        var activity: Any,
        /** in seconds */
        var executionTimeSeconds: Long = DEFAULT_EXECUTION_TIME,
        /** in seconds */
        var maxTimeDeviationSeconds: Long = DEFAULT_MAX_DEVIATION_TIME
) {
    
    constructor(activity: Any, time: Pair<Long, Long>) : this(activity, time.first, time.second)
    
    override fun toString() = activity.toString()
    
    companion object {
        /** 600 seconds */
        val DEFAULT_EXECUTION_TIME = TimeUnit.MINUTES.toSeconds(10)
        /** 120 seconds */
        val DEFAULT_MAX_DEVIATION_TIME = TimeUnit.MINUTES.toSeconds(2)
    }
}
