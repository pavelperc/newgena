package org.processmining.models.simple_behavior

import org.deckfour.xes.model.XTrace
import org.processmining.models.GenerationDescription
import org.processmining.models.MovementResult
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.WeightedPlace
import org.processmining.models.base_implementation.BaseTransition
import org.processmining.models.descriptions.GenerationDescriptionWithNoise
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.utils.LoggingSingleton
import java.util.*
import kotlin.random.Random

private typealias WPlace = WeightedPlace<Token, Place<Token>>
/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
class SimpleTransition(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        inputPlaces: List<WPlace>,
        outputPlaces: List<WPlace>,
        inputInhibitorArcPlaces: List<Place<Token>> = listOf(),
        inputResetArcPlaces: List<Place<Token>> = listOf()
) : BaseTransition(node, generationDescription, inputPlaces, outputPlaces, inputInhibitorArcPlaces, inputResetArcPlaces) {
    
    //TODO почему здесь и в версии с временами по-разному работает этот метод?
    private val noiseEventsBasedOnSettings: List<Any>
        get() {
            val noiseEvents = ArrayList<Any>()
            val noiseDescription = generationDescription.noiseDescription
            
            if (noiseDescription.isUsingInternalTransitions) {
                noiseEvents.addAll(noiseDescription.internalTransitions)
            }
            if (noiseDescription.isUsingExternalTransitions) {
                noiseEvents.addAll(noiseDescription.artificialNoiseEvents)
            }
            return noiseEvents
        }
    
    override fun move(trace: XTrace): MovementResult<*>? {
        consumeTokens()
        
        if (shouldDistortEvent()) {
            val description = generationDescription
            val noiseDescription = description.noiseDescription
            
            if (noiseDescription.isSkippingTransitions) {
                if (noiseDescription.isUsingInternalTransitions || noiseDescription.isUsingExternalTransitions) {
                    val skipEvent = Random.nextBoolean()
                    
                    if (skipEvent) {
                        //ignore this case in order to skip the event
                    } else {
                        logNoiseAndTransition(trace)
                    }
                } else {
                    //ignore this case in order to skip the event
                    // (don't use any of external/internal transitions, so just skip)
                }
            } else {
                logNoiseAndTransition(trace)
            }
        } else {
            logTransition(trace)
        }
        
        produceTokens()
        return MovementResult<Token>()
    }
    
    protected fun logNoiseAndTransition(trace: XTrace) {
        val noiseFirst = Random.nextBoolean()
        
        if (noiseFirst) {
            logNoiseEvent(trace)
            logTransition(trace)
        } else {
            // duplicate transition
            LoggingSingleton.log(trace, node)
            logTransition(trace)
        }
    }
    
    private fun logTransition(trace: XTrace) {
        val realTransition = node
        if (!realTransition.isInvisible) {
            LoggingSingleton.log(trace, node)
        }
    }
    
    protected fun logNoiseEvent(trace: XTrace) {
        val noiseEvents = noiseEventsBasedOnSettings
        if (!noiseEvents.isEmpty()) {
            val index = Random.nextInt(noiseEvents.size)
            val event = noiseEvents[index]
            LoggingSingleton.log(trace, event)
        }
    }
    
    /** @return true if we should use noise events */
    private fun shouldDistortEvent(): Boolean {
        val description = generationDescription
        if (description.isUsingNoise) {
            val noiseDescription = description.noiseDescription
            if (noiseDescription.noisedLevel >= Random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1)) {
                //use noise transitions
                return true
            }
        }
        return false
    }
    
    override val generationDescription: SimpleGenerationDescription
        get() = super.generationDescription as SimpleGenerationDescription
}
