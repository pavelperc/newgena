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
// TODO сюда добавить inhibitor and reset arcs
open class BaseTransition(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        inputPlaces: List<Place<Token>>,
        outputPlaces: List<Place<Token>>,
        inputInhibitorArcPlaces: List<Place<Token>> = listOf(),
        inputResetArcPlaces: List<Place<Token>> = listOf()
) : Transition<Place<Token>>(node, generationDescription, inputPlaces, outputPlaces, inputInhibitorArcPlaces, inputResetArcPlaces) {
    
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
        // inputInhibitorArcPlaces have no tokens
        inputResetArcPlaces.forEach { place -> while(place.hasTokens()) place.consumeToken() }
    }
}
