package org.processmining.models

import org.deckfour.xes.model.XTrace

/**
 * Created by Ivan Shugurov on 22.10.2014.
 */
interface Movable {
    fun move(trace: XTrace): MovementResult<*>?
    
    fun checkAvailability(): Boolean
}
