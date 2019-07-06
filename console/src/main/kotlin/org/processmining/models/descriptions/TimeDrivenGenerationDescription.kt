package org.processmining.models.descriptions

import com.pavelperc.newgena.models.pnmlId
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.time_driven_behavior.ResourceMapping
import java.time.Instant
import java.time.LocalDateTime
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
        
        val minimumIntervalBetweenActions: Int = 10,
        val maximumIntervalBetweenActions: Int = 20,
        
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
    
    val isUsingComplexResourceSettings = isUsingResources && isUsingComplexResourceSettings
    
    val isUsingSynchronizationOnResources = isUsingResources && isUsingSynchronizationOnResources
    
    init {
        require(minimumIntervalBetweenActions >= 0) { "minimumIntervalBetweenActions should not be negative." }
        require(maximumIntervalBetweenActions >= 0) { "maximumIntervalBetweenActions should not be negative." }
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
        val existingNoiseEvents: List<NoiseEvent> = internalTransitions.map { tr ->
            val timePair = time[tr]
                    ?: throw IllegalArgumentException("Not found time for transition ${tr.pnmlId} while making NoiseEvents.")
            NoiseEvent(tr, timePair)
        }
    }
}
