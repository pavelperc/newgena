package org.processmining.models.descriptions;

import org.processmining.models.GenerationDescription;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public abstract class BaseGenerationDescription implements GenerationDescription
{
    private int numberOfLogs = 5;
    private int numberOfTraces = 10;
    private int maxNumberOfSteps = 100;

    public int getMaxNumberOfSteps()
    {
        return maxNumberOfSteps;
    }

    public void setMaxNumberOfSteps(int maxNumberOfSteps)  //переименовать steps в transitions?
    {
        if (maxNumberOfSteps <= 0)
        {
            throw new IllegalArgumentException("Precondition violated in GenerationDescription." +
                    " Maximum number of steps must be greater than 0");
        }
        this.maxNumberOfSteps = maxNumberOfSteps;
    }

    @Override
    public int getNumberOfLogs()
    {
        return numberOfLogs;
    }

    public void setNumberOfLogs(int numberOfLogs)
    {
        if (numberOfLogs <= 0)
        {
            throw new IllegalArgumentException("Precondition violated in GenerationDescription." +
                    " Number of logs must be greater than 0");
        }
        this.numberOfLogs = numberOfLogs;
    }

    @Override
    public int getNumberOfTraces()
    {
        return numberOfTraces;
    }

    public void setNumberOfTraces(int numberOfTraces)
    {
        if (numberOfTraces <= 0)
        {
            throw new IllegalArgumentException("Precondition violated in GenerationDescription." +
                    " Number of traces must be greater than 0");
        }
        this.numberOfTraces = numberOfTraces;
    }
}
