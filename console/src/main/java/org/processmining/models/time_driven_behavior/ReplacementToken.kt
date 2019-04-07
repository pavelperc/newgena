package org.processmining.models.time_driven_behavior

import org.deckfour.xes.model.XTrace
import org.processmining.models.MovementResult
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.organizational_extension.Resource
import org.processmining.utils.TimeDrivenLoggingSingleton
import kotlin.random.Random

/**
 * Created by Ivan Shugurov on 09.10.2014.
 */
class ReplacementToken(
        private val description: TimeDrivenGenerationDescription,
        override val node: TimeDrivenTransition,
        private val recorderActivity: Any,
        resource: Resource?,
        timestamp: Long
) : TimeDrivenToken(node, timestamp, resource) {
    
    private val possibleTimeVariation: Long =
            (description.maximumIntervalBetweenActions - description.minimumIntervalBetweenActions).toLong()
    
    
    override fun copyTokenWithNewTimestamp(newTimestamp: Long) =
            ReplacementToken(description, node, recorderActivity, resource, newTimestamp)
    
    override fun move(trace: XTrace): MovementResult<*>? {
        val movementResult = MovementResult<ReplacementToken>()
        movementResult.addConsumedExtraToken(this)
        registerEvent(trace)
        
        // TODO: pavel: how much tokens should we add in ReplacementToken? and what is it.
        node.outputPlaces.forEach { (outPlace, weight) -> addTokens(outPlace, weight) }
        return movementResult
    }
    
    private fun addTokens(outPlace: Place<TimeDrivenToken>, amount: Int) {
        (1..amount).forEach { addToken(outPlace) }
    }
    
    private fun addToken(outPlace: Place<TimeDrivenToken>) {
        val timeBetweenActions = description.minimumIntervalBetweenActions +
                (Random.nextDouble() * (possibleTimeVariation + 1)).toLong()
        
        val token = TimeDrivenToken(outPlace, timestamp + timeBetweenActions * 1000)
        outPlace.addToken(token)
    }
    
    private fun registerEvent(trace: XTrace) {
        val logger = TimeDrivenLoggingSingleton.timeDrivenInstance()
        
        if (resource == null) {
            logger.log(trace, recorderActivity, timestamp, true)
        } else {
            logger.logCompleteEventWithResource(trace, recorderActivity, resource, timestamp)
        }
    }
}
