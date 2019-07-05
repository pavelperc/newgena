package org.processmining.utils.helpers

import com.pavelperc.newgena.utils.common.randomOrNull
import org.processmining.models.*

import java.util.*

/**
 * Created by Ivan Shugurov on 23.10.2014.
 */

/**
 * @param <T> type of objects which can contain tokens, for example, places in Petri nets
 * @param <K> type of objects it's possible to move through. For example, transitions in Petri nets
 * @param <F> type of additional movables possible during a replay </F></K></T>
 */
abstract class BaseGenerationHelper<T : Tokenable<*>, K : Movable, F : Movable>(
        private val initialMarking: Collection<T>,
        private val finalMarking: Collection<T>,
        allModelMovables: Collection<K>, // transitions
        val allTokenables: Collection<T>, // places
        override val generationDescription: GenerationDescription
) : GenerationHelper<K, F> {
    
    /** Transition in petrinet. Are immutable. */
    override val allModelMovables = allModelMovables.toList()
    
    /** Time driven tokens in petri net. */
    override val extraMovables = LinkedList<F>()
    
    protected abstract fun putInitialToken(place: T)
    
    init {
        moveToInitialState()
    }
    
    
    override fun moveToInitialState() {
        removeAllTokens()
        extraMovables.clear()
        for (initialTokenable in initialMarking) {
            putInitialToken(initialTokenable)
        }
    }
    
    protected fun removeAllTokens() {
        for (tokenable in allTokenables) {
            tokenable.removeAllTokens()
        }
    }
    
    
    override fun handleMovementResult(movementResult: MovementResult<F>): AssessedMovementResult {
        
        extraMovables.removeAll(movementResult.consumedExtraMovables)
        extraMovables.addAll(movementResult.producedExtraMovables)
        
        val replayCompleted = tokensOnlyInFinalMarking()
        return AssessedMovementResult(replayCompleted, true)
    }
    
    /** @return if [allTokenables] with tokens are a subset of final marking  */
    protected open fun tokensOnlyInFinalMarking(): Boolean {
        // TODO move this implementation to bpmn and make this method abstract
        return allTokenables.filter { it.hasTokens() }.all { finalMarking.contains(it) }
    }
}
