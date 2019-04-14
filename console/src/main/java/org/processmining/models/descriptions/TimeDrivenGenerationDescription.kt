package org.processmining.models.descriptions

import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.time_driven_behavior.ResourceMapping
import java.time.Instant
import java.util.*


typealias TimeNoiseDescriptionCreator 
        = TimeDrivenGenerationDescription.() -> TimeDrivenGenerationDescription.TimeNoiseDescription

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
        
        val simplifiedResources: List<Resource> = listOf(),
        override val isUsingTime: Boolean = true,
        /** transitionIdsToDelays: executionTime and maxTimeDeviation in seconds. */
        val time: Map<Transition, Pair<Long, Long>> = emptyMap(),
        override val isUsingLifecycle: Boolean = true,
        var generationStart: Instant = Instant.now(),
        val resourceMapping: Map<Any, ResourceMapping> = emptyMap(),
        val resourceGroups: List<Group> = ArrayList(),
        
        // we use lambda, because we can not instantiate default value for inner class here
        noiseDescriptionCreator: TimeNoiseDescriptionCreator = { TimeNoiseDescription() }
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
            var granularityType: GranularityTypes = GranularityTypes.MINUTES_5,
        
        
            noisedLevel: Int = 5,
            isUsingExternalTransitions: Boolean = true,
            isUsingInternalTransitions: Boolean = true,
            isSkippingTransitions: Boolean = true,
            internalTransitions: List<Transition> = emptyList(),
            artificialNoiseEvents: List<NoiseEvent> = emptyList()
    ) : GenerationDescriptionWithNoise.NoiseDescription(noisedLevel, isUsingExternalTransitions, isUsingInternalTransitions, isSkippingTransitions, internalTransitions, artificialNoiseEvents) {
        var isUsingTimestampNoise = isUsingTimestampNoise
            get() = isUsingNoise && field
        
        var isUsingLifecycleNoise = isUsingLifecycleNoise
            get() = isUsingNoise && field
        
        var isUsingTimeGranularity = isUsingTimeGranularity
            get() = isUsingNoise && field
        
        
        /** Internal transitions, wrapped to NoiseEvents. */
        val existingNoiseEvents: List<NoiseEvent> = time.map { (tr, pair) -> NoiseEvent(tr, pair) }
    }
}
