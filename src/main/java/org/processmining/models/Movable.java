package org.processmining.models;

import org.deckfour.xes.model.XTrace;

/**
 * Created by Ivan Shugurov on 22.10.2014.
 */
public interface Movable
{
    MovementResult move(XTrace trace);

    boolean checkAvailability();
}
