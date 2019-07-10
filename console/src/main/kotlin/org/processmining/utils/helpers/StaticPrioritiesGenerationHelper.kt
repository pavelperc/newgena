package org.processmining.utils.helpers

import com.pavelperc.newgena.utils.common.randomOrNull
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.Movable
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.base_implementation.BaseTransition
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.semantics.petrinet.Marking

import java.util.*

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
class StaticPrioritiesGenerationHelper protected constructor(
        initialMarking: Collection<Place<Token>>,
        finalMarking: Collection<Place<Token>>,
        allTransitions: Collection<BaseTransition>,
        allPlaces: Collection<Place<Token>>,
        description: GenerationDescriptionWithStaticPriorities,
        /** Grouped [Transition]s ascending by priorities. */
        private val priorities: SortedMap<Int, List<BaseTransition>>
) : PetriNetGenerationHelper<Place<Token>, BaseTransition, Token>(initialMarking, finalMarking, allTransitions, allPlaces, description) {
    
    /** Returns random enabled transition with maximum priority. */
    override fun chooseNextMovable(): Movable? =
            priorities.values
                    // transitions, grouped by priorities descending
                    .reversed()
                    .map { transitions ->
                        // filter enabled transitions
                        transitions.filter { it.checkAvailability() }
                    }
                    .firstOrNull { it.isNotEmpty() } // first not empty priority group
                    ?.randomOrNull()
    
    override fun putInitialToken(place: Place<Token>) {
        val token = Token()
        place.addToken(token)
    }
    
    companion object {
        
        fun createStaticPrioritiesGenerationHelper(
                petrinet: PetrinetGraph,
                initialMarking: Marking,
                finalMarking: Marking,
                description: GenerationDescriptionWithStaticPriorities
        ): StaticPrioritiesGenerationHelper {
            
            val idsToLoggablePlaces = petrinet.places.map { it.id to Place<Token>(it, description) }.toMap()
            val allPlaces = idsToLoggablePlaces.values
            
            val initialPlaces = initialMarking.mapNotNull { idsToLoggablePlaces[it.id] }
            val finalPlaces = finalMarking.mapNotNull { idsToLoggablePlaces[it.id] }
            
            val allTransitions = petrinet.transitions.map { transition ->
                val (outPlaces, inPlaces, inResetArcPlaces, inInhibitorArcPlaces)
                        = arcsToLoggablePlaces(idsToLoggablePlaces, transition, petrinet)
                
                BaseTransition(transition, description, inPlaces, outPlaces, inInhibitorArcPlaces, inResetArcPlaces)
            }
            
            
            // all transitions, sorted by priority
            val modifiedPriorities = allTransitions
                    .groupBy {
                        description.priorities[it.node]
                                ?: GenerationDescriptionWithStaticPriorities.DEFAULT_PRIORITY
                    }
                    .toSortedMap()
            
            return StaticPrioritiesGenerationHelper(initialPlaces, finalPlaces, allTransitions, allPlaces, description, modifiedPriorities)
        }
    }
}
