package org.processmining.utils.helpers

import com.pavelperc.newgena.models.pnmlId
import com.pavelperc.newgena.utils.common.randomOrNull
import org.processmining.models.*
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.models.abstract_net_representation.WeightedPlace
import org.processmining.models.graphbased.NodeID
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc

import java.util.ArrayList

/**
 * Created by Иван on 27.10.2014.
 */

abstract class PetriNetGenerationHelper<P : Place<TK>, TR : Transition<TK, P>, TK : Token>(
        initialMarking: Collection<P>,
        finalMarking: Collection<P>,
        allTransitions: Collection<TR>, // allMovables
        allPlaces: Collection<P>,// allTokenables
        description: GenerationDescription
) : BaseGenerationHelper<P, TR, TK>(initialMarking, finalMarking, allTransitions, allPlaces, description) {
    
    
    private val groupedFinalMarking = finalMarking.groupBy { it }.mapValues { it.value.size }
    
    fun dumpPetrinet(moreText: String = "") {
        println("Dump petrinet: $moreText")
        allTokenables.forEach { place ->
            println(place.node.label + ": " + place.numberOfTokens)
        }
    }
    
    
    override fun chooseNextMovable() =
            (findEnabledTransitions() + extraMovables).randomOrNull()
    
    protected fun findEnabledTransitions() = allModelMovables.filter { it.checkAvailability() }
    
    
    companion object {
        // one helper method and class for create methods:
        
        data class LoggablePlacesTuple<T : Token, P : Place<T>>(
                val outPlaces: List<WeightedPlace<T, P>>,
                val inPlaces: List<WeightedPlace<T, P>>,
                val inResetArcPlaces: List<P>,
                val inInhibitorArcPlaces: List<P>
        )
        
        /** Used in factory methods for generation helpers.
         * Converts petrinet arcs to in/out places from [idsToLoggablePlaces].*/
        fun <T : Token, P : Place<T>> arcsToLoggablePlaces(
                idsToLoggablePlaces: Map<NodeID, P>,
                transition: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
                petrinet: PetrinetGraph
        ): LoggablePlacesTuple<T, P> {
            val outPlaces = petrinet
                    .getOutEdges(transition)
                    .map { it as Arc }
                    .map { arc ->
                        val place = idsToLoggablePlaces.getValue(arc.target.id)
                        WeightedPlace(place, arc.weight)
                    }
            val inPlaces = petrinet
                    .getInEdges(transition)
                    .filter { it is Arc }
                    .map { it as Arc }
                    .map { arc ->
                        val place = idsToLoggablePlaces.getValue(arc.source.id)
                        WeightedPlace(place, arc.weight)
                    }
            
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
    
    /** Checks full match with the final marking, including the number of tokens. */
    override fun tokensOnlyInFinalMarking(): Boolean =
            allTokenables.filter { it.hasTokens() }.run {
                size == groupedFinalMarking.size
                        && all { place -> place.numberOfTokens == groupedFinalMarking.getOrDefault(place, 0) }
            }
}
