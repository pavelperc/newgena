package org.processmining.models.descriptions

import org.processmining.framework.util.Pair
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.ResourceMapping

import java.util.*

/**
 * @author Ivan Shugurov
 * Created on 11.02.2014
 */

class TimeDrivenGenerationDescription : GenerationDescriptionWithNoise() {
    
    var isSeparatingStartAndFinish = true
    
    override var isUsingResources = true
    
    var isUsingComplexResourceSettings = true
        get() = isUsingResources && field //resources with groups and roles
    
    var isUsingSynchronizationOnResources = true
        get() = isUsingResources && field
    
    //TODO стоит ли проверять разницу во времени?
    var minimumIntervalBetweenActions = 10
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Time cannot be negative")
            }
            field = value
        }
    
    //TODO стоит ли проверять разницу во времени?
    var maximumIntervalBetweenActions = 20
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Time cannot be negative")
            }
            field = value
        }
    
    val simplifiedResources: MutableList<Resource> = ArrayList()
    
    // use TimeDrivenGenerationDescription.NoiseDescription class
    override val noiseDescription = NoiseDescription(this)
    
    var time: MutableMap<Transition, Pair<Long, Long>> = mutableMapOf()
    
    val resourceGroups: List<Group> = ArrayList()
    
    val resourceMapping: MutableMap<Any, ResourceMapping> = mutableMapOf()
    
    var generationStart: Calendar = Calendar.getInstance()
    
    override val isUsingTime = true
    
    override val isUsingLifecycle: Boolean = true
    
    
    class NoiseDescription(description: TimeDrivenGenerationDescription) : GenerationDescriptionWithNoise.NoiseDescription(description) {
        var isUsingTimestampNoise = true
            get() = generationDescription.isUsingNoise && field
        
        var isUsingLifecycleNoise = true
            get() = generationDescription.isUsingNoise && field
        
        var maxTimestampDeviation: Int = 0
        
        var granularityType = GranularityTypes.MINUTES_5
        
        var isUsingTimeGranularity = true
            get() = generationDescription.isUsingNoise && field
    }
}
