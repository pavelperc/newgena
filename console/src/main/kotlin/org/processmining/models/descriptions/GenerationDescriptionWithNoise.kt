package org.processmining.models.descriptions

import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.graphbased.directed.petrinet.elements.Transition

import java.util.ArrayList

typealias NoiseDescriptionCreator = GenerationDescriptionWithNoise.() -> GenerationDescriptionWithNoise.NoiseDescription

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
abstract class GenerationDescriptionWithNoise(
        numberOfLogs: Int,
        numberOfTraces: Int,
        maxNumberOfSteps: Int,
        var isUsingNoise: Boolean,
        override var isRemovingUnfinishedTraces: Boolean,
        override var isRemovingEmptyTraces: Boolean,
        // we use lambda, because we can not instantiate default value for inner class here
        noiseDescriptionCreator: NoiseDescriptionCreator = { NoiseDescription() }
) : BaseGenerationDescription(numberOfLogs, numberOfTraces, maxNumberOfSteps) {
    
    
    
    open val noiseDescription: NoiseDescription = noiseDescriptionCreator.invoke(this)
    
    open inner class NoiseDescription(
            noisedLevel: Int = 5,
            isUsingExternalTransitions: Boolean = true,
            isUsingInternalTransitions: Boolean = true,
            var isSkippingTransitions: Boolean = true,
            val internalTransitions: List<Transition> = emptyList(),
            val artificialNoiseEvents: List<NoiseEvent> = emptyList()
    ) {
        var noisedLevel = noisedLevel
            set(value) {
                if (value < MIN_NOISE_LEVEL || value > MAX_NOISE_LEVEL) {
                    throw IllegalArgumentException("Precondition violated in TimeNoiseDescription. Unaccepted noise level")
                }
                field = value
            }
        
        // TODO: pavel: remove complex properties
        var isUsingExternalTransitions = isUsingExternalTransitions
            get () = isUsingNoise && field
        
        var isUsingInternalTransitions = isUsingInternalTransitions
            get() = isUsingNoise && field
    }
    
    companion object {
        const val MIN_NOISE_LEVEL = 1
        const val MAX_NOISE_LEVEL = 100
    }
}
