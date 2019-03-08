package org.processmining.models.bpmn_with_data;

import java.util.Random;

/**
 * Created by Ivan on 18.11.2015.
 */
public abstract class LoggableDataObject
{
    private final String label;
    protected static Random random = new Random();

    public LoggableDataObject(String label)
    {
        this.label = label;
    }

    public abstract void moveToInitialState();

    public String getLabel()
    {
        return label;
    }

    public abstract Object read();


    @Override
    public String toString()
    {
        return label;
    }
}
