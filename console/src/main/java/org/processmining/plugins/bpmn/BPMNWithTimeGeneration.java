package org.processmining.plugins.bpmn;

import com.toedter.calendar.JCalendar;
import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.BPMNWithTimeGraphSettingsListener;
import org.processmining.dialogs.BPMNWithTimePropertiesPanel;
import org.processmining.dialogs.GeneralPropertiesPanel;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.utils.helpers.BPMNWithTimeHelper;

/**
 * Created by Ivan on 10.08.2015.
 */
public class BPMNWithTimeGeneration extends BaseBPMNGenerationPlugin
{
    private BPMNWithTimeGenerationDescription description;

    @Plugin(
            name = "GENA: BPMN Log generator with time",
            returnLabels = "Event log array",
            returnTypes = EventLogArray.class,
            parameterLabels = "BPMN diagram")
    @UITopiaVariant(affiliation = "Higher School of Economics", email = "shugurov94@gmail.com", author = "Ivan Shugurov")
    public EventLogArray generate(UIPluginContext context, BPMNDiagram diagram)
    {
        setContext(context);
        description = new BPMNWithTimeGenerationDescription(diagram);

        int screenNumber = 0;
        TaskListener.InteractionResult result = TaskListener.InteractionResult.CANCEL;

        while (true)
        {
            switch (screenNumber)
            {
                case 0:
                    result = setGenerationSettings(false);
                    break;
                case 1:
                    result = configureStartDate();
                    break;
                case 3:
                    result = visualizeDiagram(diagram, true);
                    break;
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
                    BPMNWithTimeHelper helper = BPMNWithTimeHelper.creteHelper(diagram, description);
                    return generate(helper);
            }
        }

    }

    protected TaskListener.InteractionResult configureStartDate()
    {
        JCalendar calendar = new JCalendar(description.getGenerationStart());
        TaskListener.InteractionResult result = getContext().showWizard("Generation start", false, false, calendar);
        description.setGenerationStart(calendar.getCalendar());
        return result;
    }

    @Override
    protected GeneralPropertiesPanel createGeneralPropertiesPanel()
    {
        return new BPMNWithTimePropertiesPanel(this.description);
    }

    @Override
    protected ProMJGraphPanel getVisualizedDiagram(BPMNDiagram diagram)
    {
        ProMJGraphPanel graphPanel = super.getVisualizedDiagram(diagram);
        ProMJGraph graph = graphPanel.getGraph();
        BPMNWithTimeGraphSettingsListener settingsListener = new BPMNWithTimeGraphSettingsListener(description, graph);
        graph.addGraphSelectionListener(settingsListener);
        return graphPanel;
    }

    @Override
    protected GenerationDescription getGenerationDescription()
    {
        return description;
    }
}
