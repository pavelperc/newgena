package org.processmining.models.descriptions

import org.processmining.models.graphbased.directed.petrinet.elements.Transition

import java.util.HashMap

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
class GenerationDescriptionWithStaticPriorities(
        private val maxPriority: Int
) : BaseGenerationDescription() {
    
    private var priorities = mutableMapOf<Transition, Int>()
    
    override var isRemovingUnfinishedTraces = true
    
    override val isUsingTime = false
    
    override val isUsingResources: Boolean = false
    
    override val isUsingLifecycle: Boolean = false
    
    override val isRemovingEmptyTraces: Boolean = false
    
    init {
        if (maxPriority < MIN_PRIORITY) {
            throw IllegalArgumentException("Max priority cannot be less than $MIN_PRIORITY")
        }
    }
    
    fun getPriority(transition: Transition): Int {
        return priorities.getValue(transition)
    }
    
    fun putPriority(transition: Transition, priority: Int) {
        
        if (priority < MIN_PRIORITY) {
            throw IllegalArgumentException("Max priority cannot be less than $MIN_PRIORITY")
        }
        if (priority > maxPriority) {
            throw IllegalArgumentException("Max priority cannot be greater than $maxPriority")
        }
        
        priorities[transition] = priority
    }
    
    fun putPriorities(map: Map<Transition, Int>) {
        map.entries.forEach { (transition, priority) -> putPriority(transition, priority) }
    }
    
    companion object {
        val MIN_PRIORITY = 1
        
        operator fun invoke(maxPriority:Int, init: (GenerationDescriptionWithStaticPriorities.() -> Unit)) =
                GenerationDescriptionWithStaticPriorities(maxPriority).apply(init)
    }
}
