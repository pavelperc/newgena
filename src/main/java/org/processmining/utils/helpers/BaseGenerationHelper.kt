package org.processmining.utils.helpers

import org.processmining.models.*

import java.util.*

/**
 * Created by Ivan Shugurov on 23.10.2014.
 */

/**
 * @param <T> type of objects which can contain tokens, for example, places in Petri nets
 * @param <K> type of objects it's possible to move through. For example, transitions in Petri nets
 * @param <F> type of additional movables possible during a replay
</F></K></T> */
abstract class BaseGenerationHelper<T : Tokenable<*>, K : Movable, F : Movable>(
        initialMarking: Collection<T>,
        finalMarking: Collection<T>,
        allModelMovables: Collection<K>,
        allTokenables: Collection<T>,
        override val generationDescription: GenerationDescription
) : GenerationHelper<K, F> {
    
    // everything is immutable
    
    private val initialMarking = initialMarking
    private val finalMarking = finalMarking
    
    private val allTokenables = allTokenables.toList()
    
    override val allModelMovables = allModelMovables.toList()
    override val extraMovables = LinkedList<F>()
    
    init {
        moveToInitialState()
    }
    
//    override fun getGenerationDescription(): GenerationDescription {
//        return description
//    }
    
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
    
    protected fun <L : Movable> pickRandomMovable(movables: List<L>): L {
        val index = random.nextInt(movables.size)
        return movables[index]
    }
    
    
    override fun handleMovementResult(movementResult: MovementResult<F>): AssessedMovementResult {
        
        extraMovables.removeAll(movementResult.consumedExtraMovables)
        extraMovables.addAll(movementResult.producedExtraMovables)
        
        val replayCompleted = tokensOnlyInFinalMarking()
        return AssessedMovementResult(replayCompleted, true)
    }
    
    //returns true if final marking was reached
    protected open fun tokensOnlyInFinalMarking(): Boolean {
        return allTokenables.filter { it.hasTokens() }.all { finalMarking.contains(it) }
    }
    
    companion object {
        @JvmStatic
        protected val random = Random()
    }
}