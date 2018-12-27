package org.processmining.models;

import org.deckfour.xes.model.XTrace;

/**
 * Created by Ivan on 31.08.2015.
 */
public interface NodeCallback
{
    MovementResult move(XTrace trace);
}
