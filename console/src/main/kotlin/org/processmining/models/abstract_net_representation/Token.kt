package org.processmining.models.abstract_net_representation

import org.deckfour.xes.model.XTrace
import org.processmining.models.Movable
import org.processmining.models.MovementResult

/**
 * @author Ivan Shugurov
 * Created  20.07.2014
 */
open class Token : Movable {
    
    override fun move(trace: XTrace): MovementResult<*>? {
        return MovementResult<Token>()
    }
    
    override fun checkAvailability() = true
}
