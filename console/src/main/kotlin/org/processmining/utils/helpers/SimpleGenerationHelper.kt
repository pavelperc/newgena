package org.processmining.utils.helpers

import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.NodeID
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.simple_behavior.SimpleTransition

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
open class SimpleGenerationHelper protected constructor(
        initialMarking: Collection<Place<Token>>,
        finalMarking: Collection<Place<Token>>,
        allTransitions: Collection<SimpleTransition>,
        allPlaces: Collection<Place<Token>>,
        description: SimpleGenerationDescription
) : PetriNetGenerationHelper<Place<Token>, SimpleTransition, Token>(initialMarking, finalMarking, allTransitions, allPlaces, description) {
    
    override fun putInitialToken(place: Place<Token>) {
        val token = Token()
        place.addToken(token)
    }
    
    companion object {
        
        fun createHelper(
                petrinet: PetrinetGraph,
                initialMarking: Marking,
                finalMarking: Marking,
                description: SimpleGenerationDescription
        ): SimpleGenerationHelper {
            
            val idsToLoggablePlaces = petrinet.places.map { it.id to Place<Token>(it, description) }.toMap()
            
            val allPlaces = idsToLoggablePlaces.values
            
            val initialPlaces = initialMarking.map { idsToLoggablePlaces.getValue(it.id) }
            val finalPlaces = finalMarking.map { idsToLoggablePlaces.getValue(it.id) }
            
            
            val allTransitions = petrinet.transitions.map { transition ->
                
                val (outPlaces, inPlaces, inResetArcPlaces, inInhibitorArcPlaces)
                        = arcsToLoggablePlaces(idsToLoggablePlaces, transition, petrinet)
                
                
                SimpleTransition(transition, description, inPlaces, outPlaces, inInhibitorArcPlaces, inResetArcPlaces)
            }
            
            return SimpleGenerationHelper(initialPlaces, finalPlaces, allTransitions, allPlaces, description)
        }
    }
}
