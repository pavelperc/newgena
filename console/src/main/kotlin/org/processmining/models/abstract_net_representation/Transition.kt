package org.processmining.models.abstract_net_representation

import org.processmining.models.GenerationDescription
import org.processmining.models.Movable
import java.lang.IllegalArgumentException

/** Something like weighted arc. [weight] should be positive. */
data class WeightedPlace<T : Token, P : Place<T>>(val place: P, val weight: Int = 1) {
    init {
        require(weight >= 0) { "Weight should not be negative in $this" }
    }
}

/**
 * @author Ivan Shugurov
 * Created  02.12.2013
 */
//P - type of place
abstract class Transition<T : Token, P : Place<T>>(
        /** Wrapped ProM transition. (loggable.) */
        override val node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: GenerationDescription,
        val weightedInputPlaces: List<WeightedPlace<T, P>>,
        val outputPlaces: List<WeightedPlace<T, P>>,
        val inputInhibitorArcPlaces: List<P>,
        val inputResetArcPlaces: List<P>
) : AbstractPetriNode(node, generationDescription), Movable {
    
    /* Check if we can move this [Movable].*/
    override fun checkAvailability() =
            weightedInputPlaces.all { (place, weight) -> place.hasTokens(weight) } // reset arcs doesn't affect transition availability
                    && inputInhibitorArcPlaces.all { place -> !place.hasTokens() }
    
}
