package org.processmining.models.descriptions;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
public class GenerationDescriptionWithStaticPriorities extends BaseGenerationDescription
{
    public final static int MIN_PRIORITY = 1;
    private final int maxPriority;
    private final Map<Transition, Integer> priorities = new HashMap<Transition, Integer>();
    private boolean removingUnfinishedTraces = true;

    public GenerationDescriptionWithStaticPriorities(int maxPriority)
    {
        if (maxPriority < MIN_PRIORITY)
        {
            throw new IllegalArgumentException("Max priority cannot be less than " + MIN_PRIORITY);
        }
        this.maxPriority = maxPriority;
    }

    public int getPriority(Transition transition)
    {
        return priorities.get(transition);
    }

    public void putPriority(Transition transition, int priority)
    {
        if (transition == null)
        {
            throw new NullPointerException("Transition cannot equal be null");
        }
        if (priority < MIN_PRIORITY)
        {
            throw new IllegalArgumentException("Max priority cannot be less than " + MIN_PRIORITY);
        }
        if (priority > maxPriority)
        {
            throw new IllegalArgumentException("Max priority cannot be greater than " + maxPriority);
        }

        priorities.put(transition, priority);
    }

    @Override
    public boolean isUsingTime()
    {
        return false;
    }

    @Override
    public boolean isUsingResources()
    {
        return false;
    }

    @Override
    public boolean isUsingLifecycle()
    {
        return false;
    }

    @Override
    public boolean isRemovingEmptyTraces()
    {
        return false;
    }

    @Override
    public boolean isRemovingUnfinishedTraces()
    {
        return removingUnfinishedTraces;
    }

    public void setRemovingUnfinishedTraces(boolean removingUnfinishedTraces)
    {
        this.removingUnfinishedTraces = removingUnfinishedTraces;
    }
}
