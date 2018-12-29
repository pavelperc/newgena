package org.processmining.models.descriptions

import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.graphbased.directed.petrinet.elements.Transition

import java.util.ArrayList

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
abstract class GenerationDescriptionWithNoise : BaseGenerationDescription() {
    
    open val noiseDescription: NoiseDescription = NoiseDescription(this)
    
    var isUsingNoise = true
    override var isRemovingUnfinishedTraces = true
    override var isRemovingEmptyTraces = true
    
    open class NoiseDescription(protected val generationDescription: GenerationDescriptionWithNoise) {
        var noisedLevel = 5
            set(value) {
                if (value < MIN_NOISE_LEVEL || value > MAX_NOISE_LEVEL) {
                    throw IllegalArgumentException("Precondition violated in NoiseDescription. Unaccepted noise level")
                }
                field = value
            }
        
        var isUsingExternalTransitions = true
            get () = generationDescription.isUsingNoise && field
        
        var isUsingInternalTransitions = true
            get() = generationDescription.isUsingNoise && field
        
        var isSkippingTransitions = true
        
        var internalTransitions: MutableList<Transition> = ArrayList()
        
        var existingNoiseEvents: MutableList<NoiseEvent> = ArrayList()    //TODO мне не нравится название(
        
        val artificialNoiseEvents: List<NoiseEvent> = ArrayList()
        
        companion object {
            const val MIN_NOISE_LEVEL = 1
            const val MAX_NOISE_LEVEL = 100
        }
        
    }
}
