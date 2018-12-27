package org.processmining.dialogs;

import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import ru.hse.pais.shugurov.widgets.elements.ColoredSelectableElement;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.elements.SelectableElement;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class ExtendedNoiseSettingsPanel extends NoiseSettingsPanel
{
    private InputTextElement maxTimestampDeviationElement;
    private SelectableElement usingLifecycleNoiseElement;
    private SelectableElement usingTimestampNoiseElement;
    private SelectableElement usingTimeGranularityElement;

    public ExtendedNoiseSettingsPanel(TimeDrivenGenerationDescription description)
    {
        super(description);
        TimeDrivenGenerationDescription.NoiseDescription noiseDescription = description.getNoiseDescription();

        maxTimestampDeviationElement = new InputTextElement("Max timestamp deviation", Integer.toString(noiseDescription.getMaxTimestampDeviation()));
        usingTimestampNoiseElement = new ColoredSelectableElement("Use timestamp noise", noiseDescription.isUsingTimestampNoise());
        usingTimeGranularityElement = new ColoredSelectableElement("Use time granularity", noiseDescription.isUsingTimeGranularity());

        add(maxTimestampDeviationElement, 1);
        if (description.isSeparatingStartAndFinish())
        {
            usingLifecycleNoiseElement = new ColoredSelectableElement("Use noise in lifecycle", noiseDescription.isUsingLifecycleNoise());
            add(usingLifecycleNoiseElement);
        }
        add(usingTimestampNoiseElement);
        add(usingTimeGranularityElement);
    }

    @Override
    public boolean verify()
    {
        boolean isCorrect = super.verify();
        isCorrect &= verifyTimestampDeviation();
        TimeDrivenGenerationDescription generationDescription = getGenerationDescription();
        TimeDrivenGenerationDescription.NoiseDescription noiseDescription = generationDescription.getNoiseDescription();
        if (usingLifecycleNoiseElement != null)
        {
            noiseDescription.setUsingLifecycleNoise(usingLifecycleNoiseElement.isSelected());
        }
        noiseDescription.setUsingTimestampNoise(usingTimestampNoiseElement.isSelected());
        noiseDescription.setUsingTimeGranularity(usingTimeGranularityElement.isSelected());
        return isCorrect;
    }

    @Override
    protected boolean isAtLeastOneOptionSelected()
    {
        boolean isAtLeastOneOptionSelected = super.isAtLeastOneOptionSelected();
        isAtLeastOneOptionSelected |= (usingLifecycleNoiseElement != null && usingLifecycleNoiseElement.isSelected());
        isAtLeastOneOptionSelected |= usingTimestampNoiseElement.isSelected();
        isAtLeastOneOptionSelected |= usingTimeGranularityElement.isSelected();
        return isAtLeastOneOptionSelected;
    }

    private boolean verifyTimestampDeviation()
    {
        try
        {
            int maxTimestampDeviation = Integer.parseInt(maxTimestampDeviationElement.getValue());
            if (maxTimestampDeviation < 0)
            {
                return false;
            }
            else
            {
                TimeDrivenGenerationDescription description = getGenerationDescription();
                TimeDrivenGenerationDescription.NoiseDescription noiseDescription = description.getNoiseDescription();
                noiseDescription.setMaxTimestampDeviation(maxTimestampDeviation);
                return true;
            }
        } catch (NumberFormatException ex)
        {
            return false;
        }
    }
}
