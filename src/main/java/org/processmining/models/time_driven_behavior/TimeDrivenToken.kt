package org.processmining.models.time_driven_behavior

import org.deckfour.xes.model.XTrace
import org.processmining.models.MovementResult
import org.processmining.models.TokenWithTime
import org.processmining.models.abstract_net_representation.AbstractPetriNode
import org.processmining.models.organizational_extension.Resource

/**
 * Created by Ivan Shugurov on 28.10.2014.
 */
open class TimeDrivenToken @JvmOverloads constructor(
        open val node: AbstractPetriNode?,
        timestamp: Long = 0,
        val resource: Resource? = null
) : TokenWithTime(timestamp) {
    
    init {
        
        if (timestamp < 0) {
            throw IllegalArgumentException("Timestamp cannot be negative")
        }
    }
    
    override fun move(trace: XTrace): MovementResult<*>? {
        // TODO это очень грустно(
        val transition = node as? TimeDrivenTransition
                ?: throw IllegalStateException() //TODO а так вообще должно быть?
        return transition.moveInternalToken(trace, this)
    }
    
    open fun copyTokenWithNewTimestamp(newTimestamp: Long) = TimeDrivenToken(node, newTimestamp, resource)
    
    override fun toString() = "Token{node=$node}"
}
