package org.processmining.models.time_driven_behavior;

import org.processmining.framework.util.Pair;

import java.util.concurrent.TimeUnit;

/**
 * @author Ivan Shugurov
 *         Created on 03.03.2014
 */
public class NoiseEvent
{
    public static final long DEFAULT_EXECUTION_TIME = TimeUnit.MINUTES.toSeconds(10);
    public static final long DEFAULT_MAX_DEVIATION_TIME = TimeUnit.MINUTES.toSeconds(2);
    private Object activity;
    private long executionTime;
    private long maxTimeDeviation;

    public NoiseEvent(Object activity)
    {
        this.activity = activity;
        executionTime = DEFAULT_EXECUTION_TIME;
        maxTimeDeviation = DEFAULT_MAX_DEVIATION_TIME;
    }

    public NoiseEvent(Object activity, long executionTime, long maxTimeDeviation)
    {
        this.activity = activity;
        this.executionTime = executionTime;
        this.maxTimeDeviation = maxTimeDeviation;
    }

    public NoiseEvent(Object activity, Pair<Long, Long> time)
    {
        this(activity, time.getFirst(), time.getSecond());
    }

    public long getMaxTimeDeviation()
    {
        return maxTimeDeviation;
    }

    public void setMaxTimeDeviation(long maxTimeDeviation)
    {
        this.maxTimeDeviation = maxTimeDeviation;
    }

    public Object getActivity()
    {
        return activity;
    }

    public void setActivity(String activity)
    {
        this.activity = activity;
    }

    public long getExecutionTime()
    {
        return executionTime;
    }

    public void setExecutionTime(long executionTime)
    {
        this.executionTime = executionTime;
    }

    @Override
    public String toString()
    {
        return activity.toString();
    }
}
