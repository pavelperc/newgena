package org.processmining.models.base_implementation

import org.deckfour.xes.model.XTrace
import org.processmining.models.GenerationDescription
import org.processmining.models.MovementResult
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.models.abstract_net_representation.WeightedPlace
import org.processmining.models.addTokens
import org.processmining.models.consumeTokens
import org.processmining.utils.LoggingSingleton

private typealias WPlace = WeightedPlace<Token, Place<Token>>

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
open class BaseTransition(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        inputPlaces: List<WPlace>,
        outputPlaces: List<WPlace>,
        inputInhibitorArcPlaces: List<Place<Token>> = listOf(),
        inputResetArcPlaces: List<Place<Token>> = listOf()
) : Transition<Token, Place<Token>>(node, generationDescription, inputPlaces, outputPlaces, inputInhibitorArcPlaces, inputResetArcPlaces) {
    
    override fun move(trace: XTrace): MovementResult<*>? {
        consumeTokens()
        LoggingSingleton.log(trace, node)
        produceTokens()
        return MovementResult<Token>()
    }
    
    protected fun produceTokens() {
        outputPlaces.forEach { (place, weight) -> place.addTokens(List(weight) { Token() }) }
    }
    
    protected fun consumeTokens() {
        inputPlaces.forEach { (place, weight) -> place.consumeTokens(weight) }
        // inputInhibitorArcPlaces have no tokens
        inputResetArcPlaces.forEach { place -> place.removeAllTokens() }
    }
}
