package org.processmining.models.base_bpmn;

import org.processmining.models.Movable;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public abstract class Event implements Movable
{
    private final org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent;

    protected Event(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent)
    {
        this.actualEvent = actualEvent;
    }

    public org.processmining.models.graphbased.directed.bpmn.elements.Event getActualEvent()
    {
        return actualEvent;
    }

    @Override
    public String toString()
    {
        return "Loggable version of " + actualEvent;
    }
}
