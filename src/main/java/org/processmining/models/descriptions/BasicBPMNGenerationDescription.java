package org.processmining.models.descriptions;

/**
 * Created by Ivan Shugurov on 30.12.2014.
 */
public class BasicBPMNGenerationDescription extends BaseGenerationDescription
{
    private boolean usingResources;

    public BasicBPMNGenerationDescription()
    {
    }

    public BasicBPMNGenerationDescription(boolean usingResources)
    {
        this.usingResources = usingResources;
    }

    @Override
    public boolean isUsingTime()
    {
        return false;
    }

    @Override
    public boolean isUsingResources()
    {
        return usingResources;
    }

    public void setUsingResources(boolean usingResources)
    {
        this.usingResources = usingResources;
    }

    @Override
    public boolean isUsingLifecycle()
    {
        return false;
    }

    @Override
    public boolean isRemovingEmptyTraces()
    {
        return true;
    }

    @Override
    public boolean isRemovingUnfinishedTraces()
    {
        return true;
    }
}
