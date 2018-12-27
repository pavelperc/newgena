package org.processmining.plugins.petrinet;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.GeneralPropertiesPanel;
import org.processmining.dialogs.StaticPrioritiesPanel;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.utils.helpers.GenerationHelper;
import org.processmining.utils.helpers.StaticPrioritiesGenerationHelper;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
public class LogGeneratorWithStaticPriorities extends BasePetrinetGenerationPlugin
{
    private GenerationDescriptionWithStaticPriorities description;
    private Map<Transition, Integer> priorities;

    @Plugin
            (
                    name = "GENA: Log generator with static priorities",
                    returnLabels = {"Event log array", "Initial marking", "Final marking", "Generation description"},
                    returnTypes = {EventLogArray.class, Marking.class, Marking.class, GenerationDescription.class},
                    parameterLabels = "Petri net"
            )
    @UITopiaVariant
            (
                    /*affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov" */
                    affiliation = "", email = "", author = "")
    public Object[] generate(UIPluginContext context, Petrinet petrinet)
    {
        this.context = context;
        this.petrinet = petrinet;
        description = new GenerationDescriptionWithStaticPriorities(petrinet.getTransitions().size());  //TODO оставать из connection
        findMarking();

        int screenNumber = 0;
        while (true)
        {
            TaskListener.InteractionResult interactionResult;
            switch (screenNumber)
            {
                case 0:
                    interactionResult = setGenerationSettings();
                    break;
                case 1:
                    interactionResult = showPetrinet();
                    break;
                case 2:
                    interactionResult = setMarking(false);
                    break;
                case 3:
                    interactionResult = setPriorities();
                    break;
                default:
                    return generate();
            }
            switch (interactionResult)
            {
                case NEXT:
                    screenNumber++;
                    break;
                case PREV:
                    screenNumber--;
                    break;
                case CANCEL:
                    return new Object[]{EventLogArrayFactory.createEventLogArray(), new Marking(), new Marking(), null};
                case FINISHED:
                    return generate();
            }
        }
    }

    private TaskListener.InteractionResult setPriorities()
    {
        if (priorities == null)
        {
            priorities = new HashMap<Transition, Integer>();

            int defaultPriority = petrinet.getTransitions().size() / 2 + 1;

            for (Transition transition : petrinet.getTransitions())
            {
                priorities.put(transition, defaultPriority);
            }
        }
        StaticPrioritiesPanel prioritiesPanel = new StaticPrioritiesPanel(priorities);
        TaskListener.InteractionResult interactionResult;
        do
        {
            interactionResult = context.showWizard("Priorities", false, true, prioritiesPanel);
        } while (interactionResult != TaskListener.InteractionResult.CANCEL && !prioritiesPanel.verify());

        for (Map.Entry<Transition, Integer> priorityEntry : priorities.entrySet())
        {
            description.putPriority(priorityEntry.getKey(), priorityEntry.getValue());
        }

        return interactionResult;
    }

    private Object[] generate()
    {
        GenerationHelper helper = StaticPrioritiesGenerationHelper.createStaticPrioritiesGenerationHelper(petrinet,
                initialMarking, finalMarking, description);
        EventLogArray eventLogArray = generate(helper, description);
        return new Object[]{eventLogArray, initialMarking, finalMarking, description};
    }


    private TaskListener.InteractionResult setGenerationSettings()   //TODO в каждом плагине копипащу вот такой метод(
    {
        GeneralPropertiesPanel basicProperties = new GeneralPropertiesPanel(description);
        while (true)
        {
            TaskListener.InteractionResult interactionResult = context
                    .showWizard("General settings", true, false, basicProperties);
            switch (interactionResult)
            {
                case NEXT:
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

    private TaskListener.InteractionResult showPetrinet()
    {
        ProMJGraphVisualizer visualizer = ProMJGraphVisualizer.instance();
        ProMJGraphPanel visualizedNet = visualizer.visualizeGraph(context, petrinet);
        return context.showWizard("The net given", false, false, visualizedNet);
    }

}
