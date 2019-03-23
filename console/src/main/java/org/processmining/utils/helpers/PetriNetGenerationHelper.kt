package org.processmining.utils.helpers

import org.processmining.models.*
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.models.graphbased.NodeID
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc

import java.util.ArrayList

/**
 * Created by Иван on 27.10.2014.
 */

abstract class PetriNetGenerationHelper<T : Place<*>, K : Transition<*>, F : Token>(
        initialMarking: Collection<T>,
        finalMarking: Collection<T>,
        allTransitions: Collection<K>, // allMovables
        allPlaces: Collection<T>,// allTokenables
        description: GenerationDescription
) : BaseGenerationHelper<T, K, F>(initialMarking, finalMarking, allTransitions, allPlaces, description) {
    
    override fun chooseNextMovable() =
            pickRandomMovable(findEnabledTransitions() + extraMovables)
    
    protected fun findEnabledTransitions() = allModelMovables.filter { it.checkAvailability() }
    
    
    
    companion object {
        // one helper method and class for create methods:
        
        data class LoggablePlacesTuple(
                val outPlaces: List<Place<Token>>,
                val inPlaces: List<Place<Token>>,
                val inResetArcPlaces: List<Place<Token>>,
                val inInhibitorArcPlaces: List<Place<Token>>
        )
    
        /** Used in create methods for generation helpers. */
        fun arcsToLoggablePlaces(
                idsToLoggablePlaces: Map<NodeID, Place<Token>>,
                transition: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
                petrinet: PetrinetGraph
        ): LoggablePlacesTuple {
            val outPlaces = petrinet
                    .getOutEdges(transition)
                    .map { it as Arc }
                    .map { arc ->
                        val place = idsToLoggablePlaces.getValue(arc.target.id)
                        // repeating because of weight
                        List(arc.weight) { place }
                    }
                    .flatten()
        
            val inPlaces = petrinet
                    .getInEdges(transition)
                    .filter { it is Arc }
                    .map { it as Arc }
                    .map { arc ->
                        val place = idsToLoggablePlaces.getValue(arc.source.id)
                        // repeating because of weight
                        List(arc.weight) { place }
                    }
                    .flatten()
        
            val inResetArcPlaces = petrinet
                    .getInEdges(transition)
                    .filter { it is ResetArc }
                    .mapNotNull { idsToLoggablePlaces[it.source.id] }
        
            val inInhibitorArcPlaces = petrinet
                    .getInEdges(transition)
                    .filter { it is InhibitorArc }
                    .mapNotNull { idsToLoggablePlaces[it.source.id] }
        
            return LoggablePlacesTuple(outPlaces, inPlaces, inResetArcPlaces, inInhibitorArcPlaces)
        }
    }
}
