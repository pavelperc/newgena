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
        allTransitions: Collection<K>,
        allPlaces: Collection<T>,
        description: GenerationDescription
) : BaseGenerationHelper<T, K, F>(initialMarking, finalMarking, allTransitions, allPlaces, description) {
    
    override fun chooseNextMovable(): Movable? {
        val enabledTransitions = findEnabledTransitions()
        
        val movable: Movable
        
        if (enabledTransitions.isEmpty() && extraMovables.isEmpty()) {
            return null
        } else {
            if (enabledTransitions.isEmpty()) {
                movable = pickRandomMovable(extraMovables)
            } else {
                if (extraMovables.isEmpty()) {
                    movable = pickRandomMovable(enabledTransitions)
                } else {
                    val moveThroughTransition = random.nextBoolean()
                    if (moveThroughTransition) {
                        movable = pickRandomMovable(enabledTransitions)
                    } else {
                        movable = pickRandomMovable(extraMovables)
                    }
                }
            }
        }
        
        return movable
    }
    
    protected fun findEnabledTransitions() = allModelMovables.filter { it.checkAvailability() }
    
}
