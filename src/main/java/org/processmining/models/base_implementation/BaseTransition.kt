package org.processmining.models.base_implementation

import org.deckfour.xes.model.XTrace
import org.processmining.models.GenerationDescription
import org.processmining.models.MovementResult
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.utils.LoggingSingleton

import java.util.ArrayList

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
open class BaseTransition protected constructor(
        transition: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        inputPlaces: Array<Place<Token>>,
        outputPlaces: Array<Place<Token>>
) : Transition<Place<Token>>(transition, generationDescription, inputPlaces, outputPlaces) {
    
    override fun move(trace: XTrace): MovementResult<*>? {
        consumeTokens()
        LoggingSingleton.log(trace, node)
        produceTokens()
        return MovementResult<Token>()
    }
    
    protected fun produceTokens() {
        outputPlaces.forEach { place -> place.addToken(Token()) }
    }
    
    protected fun consumeTokens() {
        inputPlaces.forEach { place -> place.consumeToken() }
    }
    
    companion object {
        
        fun build(transition: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
                  description: GenerationDescription, init: BaseTransitionBuilder.() -> Unit): BaseTransition {
            return BaseTransitionBuilder(transition, description).apply { init() }.build()
        }
        
        // TODO pavel: remove builder at all
        class BaseTransitionBuilder(
                private val transition: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
                private val description: GenerationDescription/*TODO ужен свой тип?*/
        ) {
            private val inputPlaces = ArrayList<Place<Token>>()
            private val outputPlaces = ArrayList<Place<Token>>()
            
            fun inputPlace(inputPlace: Place<Token>): BaseTransitionBuilder {
                inputPlaces.add(inputPlace)
                return this
            }
            
            fun outputPlace(outputPlace: Place<Token>): BaseTransitionBuilder {
                outputPlaces.add(outputPlace)
                return this
            }
            
            fun build(): BaseTransition {
                val inputPlacesArray = inputPlaces.toTypedArray()
                
                val outputPlacesArray = outputPlaces.toTypedArray()
                
                return BaseTransition(transition, description, inputPlacesArray, outputPlacesArray)
            }
        }
    }
}
