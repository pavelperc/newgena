package org.processmining.models;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public interface GenerationDescription
{
    int getNumberOfLogs();

    void setNumberOfLogs(int numberOfLogs);

    int getNumberOfTraces();

    void setNumberOfTraces(int numberOfTraces);

    int getMaxNumberOfSteps();

    void setMaxNumberOfSteps(int maxNumberOfSteps);

    boolean isUsingTime();

    boolean isUsingResources();

    boolean isUsingLifecycle();

    boolean isRemovingEmptyTraces();

    boolean isRemovingUnfinishedTraces();
}
