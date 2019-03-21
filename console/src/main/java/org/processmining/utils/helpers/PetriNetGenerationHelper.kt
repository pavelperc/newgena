package org.processmining.utils.helpers

import org.processmining.models.*
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition

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
    
}
