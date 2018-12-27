package org.processmining.models.time.managers;

/**
 * Created by Ivan on 31.03.2016.
 */
public interface ExecutionTimeManager<T>
{
    long getExecutionTime(T object, long start);
}
