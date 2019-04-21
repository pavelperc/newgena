package org.processmining.models

import java.util.ArrayList
import java.util.Collections

/**
 * Created by Ivan Shugurov on 07.10.2014.
 */
class MovementResult<T : Movable> @JvmOverloads constructor(
        var isActualStep: Boolean = true
) {
    private val _emptiedTokenables = mutableListOf<Tokenable<*>>()
    private val _filledTokenables = mutableListOf<Tokenable<*>>()
    private val _consumedExtraMovables = mutableListOf<T>()
    private val _producedExtraMovables = mutableListOf<T>()
    
    
    val emptiedTokenables: List<Tokenable<*>>
        get() = _emptiedTokenables
     
    val filledTokenables: List<Tokenable<*>>
        get() = _filledTokenables
    
    fun addEmptiedTokenable(tokenable: Tokenable<*>) {
        _emptiedTokenables.add(tokenable)
    }
    
    fun addAllEmptiedTokenables(tokenables: Collection<Tokenable<*>>) {
        _emptiedTokenables.addAll(tokenables)
    }
    
    fun addFilledTokenables(tokenable: Tokenable<*>) {
        _filledTokenables.add(tokenable)
    }
    
    fun addAllFilledTokenables(tokenables: Collection<Tokenable<*>>) {
        _filledTokenables.addAll(tokenables)
    }
    
    fun addConsumedExtraToken(token: T) {
        _consumedExtraMovables.add(token)
    }
    
    fun addConsumedExtraTokens(token: Collection<T>) {
        _consumedExtraMovables.addAll(token)
    }
    
    fun addProducedExtraToken(movable: T) {
        _producedExtraMovables.add(movable)
    }
    
    val consumedExtraMovables: List<T>
        get() = _consumedExtraMovables
    
    val producedExtraMovables: List<T>
        get() = _producedExtraMovables
}
