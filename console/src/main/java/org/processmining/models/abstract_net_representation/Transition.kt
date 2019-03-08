package org.processmining.models.abstract_net_representation

import org.processmining.models.GenerationDescription
import org.processmining.models.Movable

import java.util.Random


/**
 * @author Ivan Shugurov
 * Created  02.12.2013
 */
//P - type of place
abstract class Transition<P : Place<*>> @JvmOverloads protected constructor(
        /** Wrapped ProM transition. */
        override val node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        val inputPlaces: List<P>,
        val outputPlaces: List<P>,
        val inputInhibitorArcPlaces: List<P> = listOf(),
        val inputResetArcPlaces: List<P> = listOf()
) : AbstractPetriNode(node, generationDescription), Movable {
    
    override fun checkAvailability() =
            (inputPlaces + inputResetArcPlaces).all { place -> place.hasTokens() } 
                    && inputInhibitorArcPlaces.all {place -> !place.hasTokens() }
    
    companion object {
        @JvmStatic
        protected val random = Random()
    }
    
}
