package org.processmining.utils.distribution;

import ru.hse.pais.shugurov.widgets.elements.InputTextElement;

/**
 * Created by Ivan on 07.12.2015.
 */
public class NormalDistributionConfigurationPanel extends DistributionConfigurationPanel
{
    private InputTextElement meanInputElement;
    private InputTextElement standardDeviationInputElement;
    private InputTextElement minInputElement;
    private InputTextElement maxInputElement;

    public NormalDistributionConfigurationPanel()
    {
        this(0, 1, -10, 10);
    }

    public NormalDistributionConfigurationPanel(double mean, double standardDeviation, double min, double max)
    {
        meanInputElement = new InputTextElement("Mean", Double.toString(mean));
        standardDeviationInputElement = new InputTextElement("Standard deviation", Double.toString(standardDeviation));
        minInputElement = new InputTextElement("Min value", Double.toString(min));
        maxInputElement = new InputTextElement("Max value", Double.toString(max));

        add(meanInputElement);
        add(standardDeviationInputElement);
        add(minInputElement);
        add(maxInputElement);
    }

    @Override
    public ConfiguredDoubleDistribution getConfiguredDistribution()
    {
        double mean;
        double standardDeviation;
        double min;
        double max;

        try
        {
            mean = Double.parseDouble(meanInputElement.getValue());
        } catch (NumberFormatException e)
        {
            return null;
        }

        try
        {
            standardDeviation = Double.parseDouble(standardDeviationInputElement.getValue());
        } catch (NumberFormatException e)
        {
            return null;
        }

        try
        {
            min = Double.parseDouble(minInputElement.getValue());
        } catch (NumberFormatException e)
        {
            return null;
        }

        try
        {
            max = Double.parseDouble(maxInputElement.getValue());
        } catch (NumberFormatException e)
        {
            return null;
        }

        if (standardDeviation < 0)
        {
            return null;
        }

        if (min > max)
        {
            return null;
        }

        NormalDistribution distribution = new NormalDistribution(mean, standardDeviation);
        return new ConfiguredDoubleDistribution(distribution, min, max);
    }
}
