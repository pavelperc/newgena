package org.processmining.models.time_driven_behavior

import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import java.util.*

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
class TimeDrivenPlace(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Place,
        generationDescription: TimeDrivenGenerationDescription
) : Place<TimeDrivenToken>(node, generationDescription) {
    
    
    /** In TimeDrivenPlace this queue is priority queue. */
    override val tokens: Queue<TimeDrivenToken> = PriorityQueue()
    
    val lowestTimestamp: Long
        get() = tokens.peek().timestamp
    
    
    override fun addToken(token: TimeDrivenToken) {
        tokens.add(token)
    }
}
