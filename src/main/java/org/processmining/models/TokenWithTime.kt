package org.processmining.models

import org.deckfour.xes.model.XTrace
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.bpmn_with_time.MovableWithTime

/**
 * Created by Ivan on 12.08.2015.
 */

open class TokenWithTime @JvmOverloads constructor(
        override val timestamp: Long,
        var callback: NodeCallback? = null
) : Token(), Comparable<TokenWithTime>, MovableWithTime {
    
    override fun compareTo(other: TokenWithTime): Int {
        return timestamp.compareTo(other.timestamp)
    }
    
    override fun move(trace: XTrace): MovementResult<*>? {
        return callback?.move(trace)
    }
}
