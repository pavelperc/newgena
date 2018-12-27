package org.processmining.plugins.bpmn;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.dialogs.GeneralPropertiesPanel;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.GeneratorWrapper;
import org.processmining.utils.helpers.GenerationHelper;

import javax.swing.*;

/**
 * Created by Ivan on 11.08.2015.
 */
public abstract class BaseBPMNGenerationPlugin
{
    private UIPluginContext context;

    protected TaskListener.InteractionResult visualizeDiagram(BPMNDiagram diagram, boolean last)
    {
        ProMJGraphPanel visualizedDiagram = getVisualizedDiagram(diagram);
        return context.showWizard("BPMN diagram", false, last, visualizedDiagram);
    }

    protected TaskListener.InteractionResult setGenerationSettings(boolean last)
    {
        GeneralPropertiesPanel basicProperties = createGeneralPropertiesPanel();

        while (true)
        {
            TaskListener.InteractionResult interactionResult = context
                    .showWizard("General settings", true, last, basicProperties);
            basicProperties.verify(); //TODO check!!!
            switch (interactionResult)
            {
                case FINISHED:
                    if (basicProperties.verify())
                    {
                        return TaskListener.InteractionResult.NEXT;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Incorrect data!");

                    }
                    break;
                default:
                    return interactionResult;
            }
        }
    }

    protected GeneralPropertiesPanel createGeneralPropertiesPanel()
    {
        return new GeneralPropertiesPanel(getGenerationDescription());
    }

    protected ProMJGraphPanel getVisualizedDiagram(BPMNDiagram diagram)
    {
        return ProMJGraphVisualizer.instance().visualizeGraph(context, diagram);
    }

    public UIPluginContext getContext()
    {
        return context;
    }

    public void setContext(UIPluginContext context)
    {
        this.context = context;
    }

    protected abstract GenerationDescription getGenerationDescription();

    public EventLogArray generate(GenerationHelper helper)
    {
        return GeneratorWrapper.generate(helper, getGenerationDescription(), context);
    }

}
