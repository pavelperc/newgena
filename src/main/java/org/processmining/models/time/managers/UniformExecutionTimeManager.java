package org.processmining.models.time.managers;

import org.processmining.utils.distribution.ConfiguredLongDistribution;

/**
 * Created by Ivan on 31.03.2016.
 */
public class UniformExecutionTimeManager<T> implements ExecutionTimeManager<T>
{
    private ConfiguredLongDistribution executionTimeDistribution;

    public UniformExecutionTimeManager(ConfiguredLongDistribution executionTimeDistribution)
    {
        this.executionTimeDistribution = executionTimeDistribution;
    }

    @Override
    public long getExecutionTime(T object, long startTime)
    {
        return executionTimeDistribution.nextLong() * 1000;
    }
}
