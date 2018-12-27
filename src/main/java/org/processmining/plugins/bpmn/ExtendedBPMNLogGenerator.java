package org.processmining.plugins.bpmn;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.BPMNGraphSettingsListener;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.ModifiedBPMNGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.utils.BPMNLoggingSingleton;
import org.processmining.utils.helpers.SimpleBPMNHelper;

/**
 * Created by Ivan Shugurov on 26.12.2014.
 */
public class ExtendedBPMNLogGenerator extends BaseBPMNGenerationPlugin
{
    private ModifiedBPMNGenerationDescription description;
    private UIPluginContext context;   //TODO delete?

    @Plugin
            (
                    name = "GENA: Extended BPMN log generator",
                    returnLabels = {"Event log array"},
                    returnTypes = {EventLogArray.class},
                    parameterLabels = "BPMN diagram"
            )
    @UITopiaVariant
            (
                    /*affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov"*/
                    affiliation = "", email = "", author = "")
    public EventLogArray generate(UIPluginContext context, BPMNDiagram diagram)
    {
        description = new ModifiedBPMNGenerationDescription(diagram, diagram.getSwimlanes().isEmpty() || !diagram.getPools().isEmpty());

        this.context = context;
        setContext(context);

        int screenNumber = 0;

        BPMNLoggingSingleton.init(description.isUsingResources());

        TaskListener.InteractionResult result;

        while (true)
        {
            switch (screenNumber)
            {
                case 0:
                    result = setGenerationSettings(false);
                    break;
                case 1:
                    result = visualizeDiagram(diagram, true);
                    break;
                default:
                    result = TaskListener.InteractionResult.FINISHED;

            }

            switch (result)
            {
                case CANCEL:
                    return null;
                case NEXT:
                    screenNumber++;
                    break;
                case PREV:
                    screenNumber--;
                    break;
                default:
                    SimpleBPMNHelper helper = SimpleBPMNHelper.createModifiedHelper(diagram, description);
                    return generate(helper);
            }
        }
    }

    @Override
    protected ProMJGraphPanel getVisualizedDiagram(BPMNDiagram diagram)
    {
        ProMJGraphPanel visualizedDiagram = super.getVisualizedDiagram(diagram);
        ProMJGraph proMJGraph = visualizedDiagram.getGraph();
        BPMNGraphSettingsListener graphSettingsListener = new BPMNGraphSettingsListener(description, proMJGraph, diagram);
        proMJGraph.addGraphSelectionListener(graphSettingsListener);

        return visualizedDiagram;
    }

    @Override
    protected GenerationDescription getGenerationDescription()
    {
        return description;
    }


}
