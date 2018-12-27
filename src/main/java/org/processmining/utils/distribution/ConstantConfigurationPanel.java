package org.processmining.utils.distribution;

import ru.hse.pais.shugurov.widgets.elements.InputTextElement;

/**
 * Created by Ivan on 10.12.2015.
 */
public class ConstantConfigurationPanel extends DistributionConfigurationPanel
{
    private InputTextElement constInput;

    public ConstantConfigurationPanel()
    {
        this(0);
    }

    public ConstantConfigurationPanel(double constValue)
    {
        constInput = new InputTextElement("Constant:", Double.toString(constValue));
        add(constInput);
    }

    @Override
    public ConfiguredDoubleDistribution getConfiguredDistribution()
    {
        try
        {
            double constant = Double.parseDouble(constInput.getValue());
            return new ConfiguredDoubleDistribution(constant);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }
}
