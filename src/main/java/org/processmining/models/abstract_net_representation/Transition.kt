package org.processmining.models.abstract_net_representation

import org.processmining.models.GenerationDescription
import org.processmining.models.Movable

import java.util.Random


/**
 * @author Ivan Shugurov
 * Created  02.12.2013
 */
//T - type of place
abstract class Transition<T : Place<*>> protected constructor(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        val inputPlaces: Array<T>,
        val outputPlaces: Array<T>
) : AbstractPetriNode(node, generationDescription), Movable {
    
    override fun checkAvailability() = inputPlaces.all { place -> place.hasTokens() }
    
    companion object {
        @JvmStatic
        protected val random = Random()
    }
    
}
