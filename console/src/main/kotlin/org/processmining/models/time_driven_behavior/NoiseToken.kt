package org.processmining.models.time_driven_behavior

import org.deckfour.xes.model.XTrace
import org.processmining.models.MovementResult
import org.processmining.models.organizational_extension.Resource
import org.processmining.utils.TimeDrivenLoggingSingleton

/**
 * @author Ivan Shugurov
 * Created  30.07.2014
 */
class NoiseToken(
        private val modelActivity: Any,
        timestamp: Long,
        resource: Resource?
) : TimeDrivenToken(null, timestamp, resource) {
    
    override fun copyTokenWithNewTimestamp(newTimestamp: Long): TimeDrivenToken {
        return NoiseToken(modelActivity, newTimestamp, resource)
    }
    
    override fun move(trace: XTrace): MovementResult<*> {
        val movementResult = MovementResult<TimeDrivenToken>()
        movementResult.addConsumedExtraToken(this)
        registerEvent(trace)
        return movementResult
    }
    
    private fun registerEvent(trace: XTrace) {
        
        val logger = TimeDrivenLoggingSingleton.timeDrivenInstance
        
        if (resource == null) {
            logger.log(trace, modelActivity, timestamp, true)
        } else {
            logger.logCompleteEventWithResource(trace, modelActivity, resource, timestamp)
        }
    }
    
}
