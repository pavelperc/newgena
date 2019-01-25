package org.processmining.models.abstract_net_representation

import org.processmining.models.GenerationDescription
import org.processmining.models.Movable

import java.util.Random


/**
 * @author Ivan Shugurov
 * Created  02.12.2013
 */
//T - type of place
abstract class Transition<T : Place<*>> @JvmOverloads protected constructor(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        val inputPlaces: List<T>,
        val outputPlaces: List<T>,
        val inputInhibitorArcPlaces: List<T> = listOf(),
        val inputResetArcPlaces: List<T> = listOf()
) : AbstractPetriNode(node, generationDescription), Movable {
    
    override fun checkAvailability() =
            (inputPlaces + inputResetArcPlaces).all { place -> place.hasTokens() } 
                    && inputInhibitorArcPlaces.all {place -> !place.hasTokens() }
    
    companion object {
        @JvmStatic
        protected val random = Random()
    }
    
}
