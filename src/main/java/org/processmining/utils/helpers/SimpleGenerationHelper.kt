package org.processmining.utils.helpers

import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.simple_behavior.SimpleTransition

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
class SimpleGenerationHelper(
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
                petrinet: Petrinet,
                initialMarking: Marking,
                finalMarking: Marking,
                description: SimpleGenerationDescription
        ): SimpleGenerationHelper {
            
            val idsToLoggablePlaces = petrinet.places.map { it.id to Place<Token>(it, description) }.toMap()
            
            val allPlaces = idsToLoggablePlaces.values
            val initialPlaces = initialMarking.mapNotNull { idsToLoggablePlaces[it.id] }
            val finalPlaces = finalMarking.mapNotNull { idsToLoggablePlaces[it.id] }
            
            
            val allTransitions = mutableListOf<SimpleTransition>()
            for (transition in petrinet.transitions) {
                val transitionBuilder = SimpleTransition.SimpleTransitionBuilder(transition, description)
                
                //gets out edges
                val outEdges = petrinet.getOutEdges(transition)
                for (edge in outEdges) {
                    val id = edge.target.id
                    val outputPlace = idsToLoggablePlaces[id]
                    transitionBuilder.outputPlace(outputPlace)
                }
                
                //get in edges
                val inEdges = petrinet.getInEdges(transition)
                for (edge in inEdges) {
                    val id = edge.source.id
                    val inputPlace = idsToLoggablePlaces[id]
                    transitionBuilder.inputPlace(inputPlace)
                }
                
                val simpleTransition = transitionBuilder.build()
                allTransitions.add(simpleTransition)
            }
            return SimpleGenerationHelper(initialPlaces, finalPlaces, allTransitions, allPlaces, description)
        }
    }
}
