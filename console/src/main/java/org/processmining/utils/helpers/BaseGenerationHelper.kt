package org.processmining.utils.helpers

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
        allModelMovables: Collection<K>,
        protected val allTokenables: Collection<T>,
        override val generationDescription: GenerationDescription
) : GenerationHelper<K, F> {
    
    // allModelMovables is immutable
    override val allModelMovables = allModelMovables.toList()
    
    override val extraMovables = LinkedList<F>()
    
    init {
        moveToInitialState()
    }
    
    protected abstract fun putInitialToken(place: T)
    
    
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
    
    protected fun <L : Movable> pickRandomMovable(movables: List<L>): L? =
            if (movables.isEmpty())
                null
            else
                movables.random()
    
    
    override fun handleMovementResult(movementResult: MovementResult<F>): AssessedMovementResult {
        
        extraMovables.removeAll(movementResult.consumedExtraMovables)
        extraMovables.addAll(movementResult.producedExtraMovables)
        
        val replayCompleted = tokensOnlyInFinalMarking()
        return AssessedMovementResult(replayCompleted, true)
    }
    
    /** @return if [allTokenables] with tokens are a subset of final marking  */
    protected open fun tokensOnlyInFinalMarking(): Boolean {// TODO move this implementation to bpmn
        return allTokenables.filter { it.hasTokens() }.all { finalMarking.contains(it) }
    }
    
    companion object {
        @JvmStatic
        protected val random = Random()
    }
}
