package org.processmining.utils.distribution;

import ru.hse.pais.shugurov.widgets.elements.InputTextElement;

/**
 * Created by Ivan on 07.12.2015.
 */
public class UniformDistributionConfigurationPanel extends DistributionConfigurationPanel
{
    private UniformDistribution distribution;
    private InputTextElement minInputElement;
    private InputTextElement maxInputElement;

    public UniformDistributionConfigurationPanel()
    {
        this(-10, 10);
    }

    public UniformDistributionConfigurationPanel(double min, double max)
    {
        distribution = new UniformDistribution();
        minInputElement = new InputTextElement("Min value:", Double.toString(min));
        maxInputElement = new InputTextElement("Max value:", Double.toString(max));

        add(minInputElement);
        add(maxInputElement);
    }


    @Override
    public ConfiguredDoubleDistribution getConfiguredDistribution()
    {
        double min;

        try
        {
            min = Double.parseDouble(minInputElement.getValue());
        } catch (NumberFormatException e)
        {
            return null;
        }

        double max;

        try
        {
            max = Double.parseDouble(maxInputElement.getValue());
        } catch (NumberFormatException e)
        {
            return null;
        }


        if (min > max)
        {
            return null;
        }

        return new ConfiguredDoubleDistribution(distribution, min, max);
    }
}
