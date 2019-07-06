package org.processmining.models.descriptions

import com.pavelperc.newgena.models.pnmlId
import org.processmining.models.graphbased.directed.petrinet.elements.Transition

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
open class GenerationDescriptionWithStaticPriorities(
        numberOfLogs: Int = 5,
        numberOfTraces: Int = 10,
        maxNumberOfSteps: Int = 100,
        
        override var isRemovingUnfinishedTraces: Boolean = true,
        override var isRemovingEmptyTraces: Boolean = false,
        val priorities: Map<Transition, Int> = mapOf()
) : BaseGenerationDescription(numberOfLogs, numberOfTraces, maxNumberOfSteps) {
    
    override val isUsingTime: Boolean = false
    override val isUsingResources: Boolean = false
    override val isUsingLifecycle: Boolean = false
    
    init {
        priorities.forEach { tr, priority ->
            require(priority >= 1) { "Priority for transition ${tr.pnmlId} should be greater than 0." }
        }
    }
    
    companion object {
        const val DEFAULT_PRIORITY = 1
    }
}
