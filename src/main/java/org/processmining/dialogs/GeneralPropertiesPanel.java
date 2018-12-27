package org.processmining.dialogs;

import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.GenerationDescriptionWithNoise;
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities;
import ru.hse.pais.shugurov.widgets.elements.ColoredSelectableElement;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.elements.SelectableElement;
import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Ivan Shugurov
 *         Created on 16.02.14
 */
public class GeneralPropertiesPanel extends EmptyPanel
{
    private final InputTextElement numberOfLogsElement;
    private final InputTextElement numberOfTracesElement;
    private final InputTextElement maximumNumberOfStepsElement;
    private final GenerationDescription description;

    public GeneralPropertiesPanel(final GenerationDescription description)
    {
        this.description = description;
        numberOfLogsElement = new InputTextElement("Number of logs", Integer.toString(description.getNumberOfLogs()));

        numberOfTracesElement = new InputTextElement("Number of traces",
                Integer.toString(description.getNumberOfTraces()));

        maximumNumberOfStepsElement = new InputTextElement("Maximum number of steps",
                Integer.toString(description.getMaxNumberOfSteps()));

        add(numberOfLogsElement);
        add(numberOfTracesElement);
        add(maximumNumberOfStepsElement);
    }

    public GeneralPropertiesPanel(final GenerationDescriptionWithNoise description)
    {
        this((GenerationDescription) description);

        final SelectableElement removeUnfinishedTracesElement = new ColoredSelectableElement("Remove unfinished traces", description.isRemovingUnfinishedTraces());
        removeUnfinishedTracesElement.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                description.setRemovingUnfinishedTraces(removeUnfinishedTracesElement.isSelected());
            }
        });
        add(removeUnfinishedTracesElement);
    }

    public GeneralPropertiesPanel(final GenerationDescriptionWithStaticPriorities description)
    {
        this((GenerationDescription) description);

        final SelectableElement removeUnfinishedTracesElement = new ColoredSelectableElement("Remove unfinished traces", description.isRemovingUnfinishedTraces());
        removeUnfinishedTracesElement.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                description.setRemovingUnfinishedTraces(removeUnfinishedTracesElement.isSelected());
            }
        });
        add(removeUnfinishedTracesElement);
    }

    protected GenerationDescription getGenerationDescription()
    {
        return description;
    }

    public boolean verify()
    {
        boolean isCorrect = checkNumberOfLogs();
        isCorrect &= checkNumberOfTraces();
        isCorrect &= checkNumberOfSteps();
        return isCorrect;
    }

    private boolean checkNumberOfSteps()
    {
        boolean isCorrect = true;
        int maximumNumberOfSteps = -1;
        try
        {
            maximumNumberOfSteps = Integer.parseInt(maximumNumberOfStepsElement.getValue());
        } catch (NumberFormatException e)
        {
            isCorrect = false;
        }
        if (maximumNumberOfSteps <= 0)
        {
            isCorrect = false;
        }
        else
        {
            description.setMaxNumberOfSteps(maximumNumberOfSteps);
        }
        return isCorrect;
    }

    private boolean checkNumberOfTraces()
    {
        boolean isCorrect = true;
        int numberOfTraces = -1;
        try
        {
            numberOfTraces = Integer.parseInt(numberOfTracesElement.getValue());
        } catch (NumberFormatException e)
        {
            isCorrect = false;
        }
        if (numberOfTraces <= 0)
        {
            isCorrect = false;
        }
        else
        {
            description.setNumberOfTraces(numberOfTraces);
        }
        return isCorrect;
    }

    private boolean checkNumberOfLogs()
    {
        boolean isCorrect = true;
        int numberOfLogs = -1;
        try
        {
            numberOfLogs = Integer.parseInt(numberOfLogsElement.getValue());
        } catch (NumberFormatException e)
        {
            isCorrect = false;
        }
        if (numberOfLogs <= 0)
        {
            isCorrect = false;
        }
        else
        {
            description.setNumberOfLogs(numberOfLogs);
        }
        return isCorrect;
    }

}
