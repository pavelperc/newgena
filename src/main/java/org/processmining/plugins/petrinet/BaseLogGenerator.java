package org.processmining.plugins.petrinet;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ArtificialNoisePanel;
import org.processmining.dialogs.GeneralPropertiesPanel;
import org.processmining.dialogs.NoiseSettingsPanel;
import org.processmining.dialogs.PropertiesPanelWithNoise;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.GenerationDescriptionWithNoise;
import org.processmining.models.descriptions.SimpleGenerationDescription;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.time_driven_behavior.NoiseEvent;
import org.processmining.utils.helpers.SimpleGenerationHelper;
import ru.hse.pais.shugurov.widgets.panels.MultipleChoicePanel;

import javax.swing.*;
import java.util.List;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class BaseLogGenerator extends BasePetrinetGenerationPlugin
{
    private SimpleGenerationDescription description;
    private int noiseState = 0; //should be used only within configureNoise()

    @Plugin
            (
                    name = "GENA: Simple log generator",
                    returnLabels = {"Event log array", "Initial marking", "Final marking", "Generation description"},
                    returnTypes = {EventLogArray.class, Marking.class, Marking.class, GenerationDescription.class},
                    parameterLabels = "Petri net"
            )
    @UITopiaVariant
            (
                    affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov" )
    public Object[] generate(UIPluginContext context, Petrinet petrinet)
    {
        this.context = context;
        description = new SimpleGenerationDescription();
        this.petrinet = petrinet;
        findMarking();

        int screenNumber = 0;
        TaskListener.InteractionResult interactionResult = TaskListener.InteractionResult.CANCEL;
        while (true)
        {
            switch (screenNumber)
            {
                case 0:
                    interactionResult = setGenerationSettings();
                    break;
                case 1:
                    interactionResult = showPetrinet();
                    break;
                case 2:
                    interactionResult = setMarking(!description.isUsingNoise());
                    break;
                case 3:
                    if (description.isUsingNoise())
                    {
                        interactionResult = configureNoise(true);
                    }
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
                    Object[] results = generate();
                    EventLogArray logArray = (EventLogArray) results[0];
                    XLog log = logArray.getLog(0);

                    /*try
                    {
                        //TODO comment it
                        ExportLogXes.export(log, new File("example.xes"));
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }   */

                    return results;
            }
        }
    }

    protected Object[] generate()
    {
        SimpleGenerationHelper generationHelper = SimpleGenerationHelper.createHelper(petrinet, initialMarking, finalMarking, description);
        EventLogArray logArray = generate(generationHelper, description);
        return new Object[]{logArray, initialMarking, finalMarking, description};
    }

    private TaskListener.InteractionResult setGenerationSettings()
    {
        GeneralPropertiesPanel basicProperties = new PropertiesPanelWithNoise(description);
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

    private TaskListener.InteractionResult setNoiseSettings()
    {
        while (true)
        {
            NoiseSettingsPanel panel = new NoiseSettingsPanel(description);
            TaskListener.InteractionResult result = context.showWizard("Noise settings", false, false, panel);
            switch (result)
            {
                case CANCEL:
                    return TaskListener.InteractionResult.CANCEL;
                case NEXT:
                    if (panel.verify())
                    {
                        return TaskListener.InteractionResult.NEXT;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Incorrect data occurs");
                    }

                    break;
                case PREV:
                    if (panel.verify())
                    {
                        return TaskListener.InteractionResult.PREV;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Incorrect data occurs");
                    }
            }
        }
    }

    private TaskListener.InteractionResult configureNoise(boolean isLast)  //TODO проблема с графикой, если выбрать только пропускание элементов
    {
        //TODO отрефакторить момент с определением последнего окошка? а надо ли вообще?
        GenerationDescriptionWithNoise.NoiseDescription noiseDescription = description.getNoiseDescription();
        TaskListener.InteractionResult result = TaskListener.InteractionResult.PREV;
        while (true)
        {
            switch (noiseState)
            {
                case -1:
                    noiseState = 0;
                    return result;
                case 0:     //view with general settings
                    result = setNoiseSettings();
                    break;
                case 1:     //view with internal transitions
                    if (noiseDescription.isUsingInternalTransitions())
                    {
                        result = configureExistentNoiseTransitions(isLast && !noiseDescription.isUsingExternalTransitions());
                    }
                    else
                    {
                        if (result == TaskListener.InteractionResult.PREV)
                        {
                            noiseState--;
                        }
                        else
                        {
                            noiseState++;
                        }
                        continue;
                    }
                    break;
                case 2:     //view with external transitions
                    if (noiseDescription.isUsingExternalTransitions())
                    {
                        result = configureArtificialNoiseTransitions(isLast);
                    }
                    else
                    {
                        if (result == TaskListener.InteractionResult.PREV)
                        {
                            noiseState--;
                        }
                        else
                        {
                            noiseState++;
                        }
                        continue;
                    }
                    break;
                case 3:
                    noiseState--;
                    return result;
            }
            switch (result)
            {
                case NEXT:
                    noiseState++;
                    break;
                case PREV:
                    noiseState--;
                    break;
                default:
                    return result;
            }
        }
    }

    private TaskListener.InteractionResult configureExistentNoiseTransitions(boolean isLast)
    {
        MultipleChoicePanel<Transition> panel;
        if (description.getNoiseDescription().getInternalTransitions() == null)
        {
            panel = new MultipleChoicePanel<>(petrinet.getTransitions());
        }
        else
        {
            panel = new MultipleChoicePanel<>(petrinet.getTransitions(), description.getNoiseDescription().getInternalTransitions());
        }
        TaskListener.InteractionResult result = context.showWizard("Noise made of transitions represented in the Petri net", false, isLast, panel);
        description.getNoiseDescription().setInternalTransitions(panel.getChosenOptionsAsList());
        return result;
    }

    private TaskListener.InteractionResult configureArtificialNoiseTransitions(boolean isLast)
    {
        List<NoiseEvent> artificialNoiseEvents = description.getNoiseDescription().getArtificialNoiseEvents();
        ArtificialNoisePanel panel = new ArtificialNoisePanel(artificialNoiseEvents, description.isUsingTime());
        return context.showWizard("Create artificial events", false, isLast, panel);
    }
}
