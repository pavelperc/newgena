package org.processmining.models.abstract_net_representation;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.Movable;
import org.processmining.models.MovementResult;

/**
 * @author Ivan Shugurov
 *         Created  20.07.2014
 */
public class Token implements Movable
{
    public Token()
    {
    }

    public MovementResult move(XTrace trace)
    {
        return new MovementResult();
    }

    @Override
    public boolean checkAvailability()
    {
        return true;
    }
}
