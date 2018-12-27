package org.processmining.dialogs;


import org.processmining.models.descriptions.GenerationDescriptionWithNoise;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import ru.hse.pais.shugurov.widgets.elements.ColoredSelectableElement;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.elements.SelectableElement;
import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;

/**
 * @author Ivan Shugurov
 *         Created  25.02.2014
 */
public class NoiseSettingsPanel extends EmptyPanel
{
    private GenerationDescriptionWithNoise description;
    private InputTextElement thresholdLevelElement;
    private SelectableElement usingExternalTransitionsElement;
    private SelectableElement usingInternalTransitionsElement;
    private SelectableElement isSkippingTransitionsElement;

    public NoiseSettingsPanel(GenerationDescriptionWithNoise description)
    {
        this.description = description;
        GenerationDescriptionWithNoise.NoiseDescription noiseDescription = description.getNoiseDescription();
        thresholdLevelElement = new InputTextElement("Noise level", Integer.toString(noiseDescription.getNoisedLevel()));
        usingExternalTransitionsElement = new ColoredSelectableElement("Use artificial events", noiseDescription.isUsingExternalTransitions());
        usingInternalTransitionsElement = new ColoredSelectableElement("Use internal transitions", noiseDescription.isUsingInternalTransitions());
        isSkippingTransitionsElement = new ColoredSelectableElement("Use skipping events", noiseDescription.isSkippingTransitions());

        add(thresholdLevelElement);
        add(usingInternalTransitionsElement);
        add(usingExternalTransitionsElement);
        add(isSkippingTransitionsElement);
    }

    protected TimeDrivenGenerationDescription getGenerationDescription()
    {
        return (TimeDrivenGenerationDescription) description;
    }

    public boolean verify()
    {
        boolean isCorrect = verifyThresholdLevel();
        isCorrect &= verifySelectableOptions();
        return isCorrect;
    }

    private boolean verifySelectableOptions()
    {
        boolean isCorrect = true;
        if (isAtLeastOneOptionSelected())
        {
            GenerationDescriptionWithNoise.NoiseDescription noiseDescription = description.getNoiseDescription();
            noiseDescription.setUsingExternalTransitions(usingExternalTransitionsElement.isSelected());
            noiseDescription.setUsingInternalTransitions(usingInternalTransitionsElement.isSelected());
            noiseDescription.setSkippingTransitions(isSkippingTransitionsElement.isSelected());
        }
        else
        {
            isCorrect = false;
        }
        return isCorrect;
    }

    protected boolean isAtLeastOneOptionSelected()
    {
        return usingExternalTransitionsElement.isSelected() || usingInternalTransitionsElement.isSelected() || isSkippingTransitionsElement.isSelected();
    }

    private boolean verifyThresholdLevel()
    {
        boolean isCorrect = true;
        try
        {
            int thresholdLevel = Integer.parseInt(thresholdLevelElement.getValue());
            if (thresholdLevel < TimeDrivenGenerationDescription.NoiseDescription.MIN_NOISE_LEVEL ||
                    thresholdLevel > TimeDrivenGenerationDescription.NoiseDescription.MAX_NOISE_LEVEL)
            {
                isCorrect = false;
            }
            else
            {
                description.getNoiseDescription().setNoisedLevel(thresholdLevel);
            }

        } catch (NumberFormatException e)
        {
            isCorrect = false;
        }
        return isCorrect;
    }
}
