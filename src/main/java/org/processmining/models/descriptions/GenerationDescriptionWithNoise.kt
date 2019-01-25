package org.processmining.models.descriptions

import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.graphbased.directed.petrinet.elements.Transition

import java.util.ArrayList

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
        noiseDescriptionCreator: (GenerationDescriptionWithNoise.() -> GenerationDescriptionWithNoise.NoiseDescription) =
                { NoiseDescription() }
) : BaseGenerationDescription(numberOfLogs, numberOfTraces, maxNumberOfSteps) {
    
    open val noiseDescription: NoiseDescription = noiseDescriptionCreator.invoke(this)
    
    open inner class NoiseDescription(
            noisedLevel: Int = 5,
            isUsingExternalTransitions: Boolean = true,
            isUsingInternalTransitions: Boolean = true,
            var isSkippingTransitions: Boolean = true,
            var internalTransitions: MutableList<Transition> = ArrayList(),
            var existingNoiseEvents: MutableList<NoiseEvent> = ArrayList()    //TODO мне не нравится название(
    ) {
        var noisedLevel = noisedLevel
            set(value) {
                if (value < MIN_NOISE_LEVEL || value > MAX_NOISE_LEVEL) {
                    throw IllegalArgumentException("Precondition violated in TimeNoiseDescription. Unaccepted noise level")
                }
                field = value
            }
        
        var isUsingExternalTransitions = isUsingExternalTransitions
            get () = isUsingNoise && field
        
        var isUsingInternalTransitions = isUsingInternalTransitions
            get() = isUsingNoise && field
        
        
        val artificialNoiseEvents: List<NoiseEvent> = mutableListOf()
    }
    
    companion object{
        const val MIN_NOISE_LEVEL = 1
        const val MAX_NOISE_LEVEL = 100
    }
}
