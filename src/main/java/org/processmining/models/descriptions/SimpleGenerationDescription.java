package org.processmining.models.descriptions;

/**
 * Created by Ivan Shugurov on 12.11.2014.
 */
public class SimpleGenerationDescription extends GenerationDescriptionWithNoise
{
    public SimpleGenerationDescription()
    {
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

}
