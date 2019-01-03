package org.processmining.models.descriptions

import com.sun.org.apache.xpath.internal.operations.Bool
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

class TimeDrivenGenerationDescription(
        numberOfLogs: Int = 5,
        numberOfTraces: Int = 10,
        maxNumberOfSteps: Int = 100,
        
        isUsingNoise: Boolean = true,
        override var isUsingResources: Boolean = true,
        
        isRemovingUnfinishedTraces: Boolean = true,
        isRemovingEmptyTraces: Boolean = true,
        isUsingComplexResourceSettings: Boolean = true,
        isUsingSynchronizationOnResources: Boolean = true,
        
        minimumIntervalBetweenActions: Int = 10,
        maximumIntervalBetweenActions: Int = 20,
        
        var isSeparatingStartAndFinish: Boolean = true,
        
        val simplifiedResources: MutableList<Resource> = ArrayList(),
        var time: MutableMap<Transition, Pair<Long, Long>> = mutableMapOf(),
        override val isUsingTime: Boolean = true,
        override val isUsingLifecycle: Boolean = true,
        var generationStart: Calendar = Calendar.getInstance(),
        val resourceMapping: MutableMap<Any, ResourceMapping> = mutableMapOf(),
        val resourceGroups: List<Group> = ArrayList(),
        
        // we use lambda, because we can not instantiate default value for inner class here
        noiseDescriptionCreator: TimeDrivenGenerationDescription.() -> TimeNoiseDescription = { TimeNoiseDescription() }
) : GenerationDescriptionWithNoise(numberOfLogs, numberOfTraces, maxNumberOfSteps, isUsingNoise, isRemovingUnfinishedTraces, isRemovingEmptyTraces) {
    
    override val noiseDescription: TimeNoiseDescription = noiseDescriptionCreator.invoke(this)
    
    var isUsingComplexResourceSettings = isUsingComplexResourceSettings
        get() = isUsingResources && field //resources with groups and roles
    
    var isUsingSynchronizationOnResources = isUsingSynchronizationOnResources
        get() = isUsingResources && field
    
    //TODO стоит ли проверять разницу во времени?
    var minimumIntervalBetweenActions = minimumIntervalBetweenActions
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Time cannot be negative")
            }
            field = value
        }
    
    //TODO стоит ли проверять разницу во времени?
    var maximumIntervalBetweenActions = maximumIntervalBetweenActions
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Time cannot be negative")
            }
            field = value
        }
    
    inner class TimeNoiseDescription(
            isUsingTimestampNoise: Boolean = true,
            isUsingLifecycleNoise: Boolean = true,
            isUsingTimeGranularity: Boolean = true,
            var maxTimestampDeviation: Int = 0,
            var granularityType: GranularityTypes = GranularityTypes.MINUTES_5
    ) : GenerationDescriptionWithNoise.NoiseDescription() {
        var isUsingTimestampNoise = isUsingTimestampNoise
            get() = isUsingNoise && field
        
        var isUsingLifecycleNoise = isUsingLifecycleNoise
            get() = isUsingNoise && field
        
        var isUsingTimeGranularity = isUsingTimeGranularity
            get() = isUsingNoise && field
    }
}
