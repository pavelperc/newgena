package org.processmining.utils.helpers

import org.processmining.models.AssessedMovementResult
import org.processmining.models.GenerationDescription
import org.processmining.models.Movable
import org.processmining.models.MovementResult

/**
 * Created by Ivan Shugurov on 22.10.2014.
 */
interface GenerationHelper<K : Movable, F : Movable> {
    val generationDescription: GenerationDescription
    
    val allModelMovables: List<K>
    
    val extraMovables: List<F>
    
    fun moveToInitialState()
    
    fun chooseNextMovable(): Movable?
    
    /*returns true if final marking was reached*/
    fun handleMovementResult(movementResult: MovementResult<F>): AssessedMovementResult
}
