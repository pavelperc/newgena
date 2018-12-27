package org.processmining.utils.helpers;

import org.processmining.models.AssessedMovementResult;
import org.processmining.models.GenerationDescription;
import org.processmining.models.Movable;
import org.processmining.models.MovementResult;

import java.util.List;

/**
 * Created by Ivan Shugurov on 22.10.2014.
 */
public interface GenerationHelper<K extends Movable, F extends Movable>
{
    GenerationDescription getGenerationDescription();

    List<K> getAllModelMovables();

    List<F> getExtraMovables();

    void moveToInitialState();

    Movable chooseNextMovable();

    /*returns true if final marking was reached*/
    AssessedMovementResult handleMovementResult(MovementResult movementResult);
}
