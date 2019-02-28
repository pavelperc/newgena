package org.processmining.models.descriptions

import org.processmining.models.graphbased.directed.petrinet.elements.Transition

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
open class GenerationDescriptionWithStaticPriorities(
        private val maxPriority: Int,
        
        numberOfLogs: Int = 5,
        numberOfTraces: Int = 10,
        maxNumberOfSteps: Int = 100,
        
        override var isRemovingUnfinishedTraces: Boolean = true,
        priorities: Map<Transition, Int> = mapOf()
) : BaseGenerationDescription(numberOfLogs, numberOfTraces, maxNumberOfSteps) {
    
    override val isUsingTime: Boolean = false
    override val isUsingResources: Boolean = false
    override val isUsingLifecycle: Boolean = false
    override val isRemovingEmptyTraces: Boolean = false
    
    // to avoid @JvmOverloads
    constructor(maxPriority: Int) : this(maxPriority, 5)
    
    
    private var _priorities = mutableMapOf<Transition, Int>()
    
    // read-only
    val priorities
        get() = _priorities as Map<Transition, Int>
    
    init {
        if (maxPriority < MIN_PRIORITY) {
            throw IllegalArgumentException("Max priority cannot be less than $MIN_PRIORITY")
        }
        putPriorities(priorities)
    }
    
    fun putPriority(transition: Transition, priority: Int) {
        if (priority < MIN_PRIORITY) {
            throw IllegalArgumentException("Max priority cannot be less than $MIN_PRIORITY")
        }
        if (priority > maxPriority) {
            throw IllegalArgumentException("Max priority cannot be greater than $maxPriority")
        }
        
        _priorities[transition] = priority
    }
    
    fun putPriorities(map: Map<Transition, Int>) {
        map.entries.forEach { (transition, priority) -> putPriority(transition, priority) }
    }
    
    
    companion object {
        val MIN_PRIORITY = 1
    }
}
