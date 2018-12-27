package org.processmining.plugins.petrinet;

import com.toedter.calendar.JCalendar;
import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.connections.LogGeneratorConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.*;
import org.processmining.dialogs.organizational_extension.*;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.organizational_extension.Role;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.time_driven_behavior.GranularityTypes;
import org.processmining.models.time_driven_behavior.NoiseEvent;
import org.processmining.models.time_driven_behavior.ResourceMapping;
import org.processmining.utils.Generator;
import org.processmining.utils.TimeDrivenLoggingSingleton;
import org.processmining.utils.helpers.GenerationHelper;
import org.processmining.utils.helpers.TimeDrivenGenerationHelper;
import ru.hse.pais.shugurov.widgets.panels.MultipleChoicePanel;
import ru.hse.pais.shugurov.widgets.panels.SingleChoicePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Shugurov
 */

public class TimeDrivenLogGenerator extends BasePetrinetGenerationPlugin //TODO если пропускаю событий, то не показывается кнопка "finish"
{
    private TimeDrivenGenerationDescription description;
    private int noiseState = 0; //should be used only within configureNoise()
    private int resourceScreenIndex = 0; // should be used only within configureResources();
    private int indexOfGroupWithinRoleCreation = 0;  //should be used only within createResources

    @Plugin

            (
                    name = "GENA: Time-Driven log generator",
                    returnLabels = {"Event log array", "Initial marking", "Final marking", "Generation description"},
                    returnTypes = {org.processmining.log.models.EventLogArray.class, Marking.class, Marking.class, GenerationDescription.class},
                    parameterLabels = {"Petri net"}
            )
    @UITopiaVariant
            (
                    affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov")
    public Object[] generate(UIPluginContext context, Petrinet petrinet)
    {
        this.petrinet = petrinet;
        this.context = context;

        Generator generator;
        try
        {
            LogGeneratorConnection connection = context.getConnectionManager()
                    .getFirstConnection(LogGeneratorConnection.class, context, petrinet);
            description = connection.getGenerationDescription();
        } catch (ConnectionCannotBeObtained connectionCannotBeObtained)
        {
            description = new TimeDrivenGenerationDescription();
            Map<Transition, Pair<Long, Long>> timeMap = description.getTime();
            for (Transition transition : petrinet
                    .getTransitions())
            {
                timeMap.put(transition, new Pair<>(7200L, 1000L));
            }
        }

        TimeDrivenLoggingSingleton.init(description);

        findMarking();

        int state = 0;
        int sectionsTillEnd = 0;
        int numberOfOptionalScreens = 0;
        TaskListener.InteractionResult result = TaskListener.InteractionResult.CANCEL;
        boolean isSkipable = false;

        while (true)
        {
            switch (state)
            {
                case 0:   //basic settings
                    result = setGenerationSettings();
                    sectionsTillEnd = 0;
                    if (description.isUsingNoise())
                    {
                        sectionsTillEnd++;
                    }
                    if (description.isUsingResources())
                    {
                        sectionsTillEnd++;
                    }
                    numberOfOptionalScreens = sectionsTillEnd;
                    break;
                case 1:
                    result = configureStartDate();
                    break;
                case 2:   //visualize petrinet
                    result = showPetrinet();
                    break;
                case 3:   //set marking
                    result = setMarking(sectionsTillEnd == 0);
                    break;
                case 4:    //noise
                    if (description.isUsingNoise())
                    {
                        sectionsTillEnd--;
                        isSkipable = true;
                        result = configureNoise(sectionsTillEnd == 0);
                    }
                    break;
                case 5:
                    if (description.isUsingResources())
                    {
                        sectionsTillEnd--;
                        isSkipable = true;
                        result = configureResources();
                    }
                    break;
                default:
                    result = TaskListener.InteractionResult.FINISHED;
            }

            switch (result)
            {
                case NEXT:
                    state++;
                    isSkipable = false;
                    break;
                case PREV:
                    state--;
                    if (isSkipable)
                    {
                        sectionsTillEnd += 2;
                        if (sectionsTillEnd > numberOfOptionalScreens)
                        {
                            sectionsTillEnd = numberOfOptionalScreens;

                        }
                        isSkipable = false;
                    }
                    break;
                case CANCEL:
                    return new Object[]{null, null, null, null};
                case FINISHED:
                    List<NoiseEvent> timedInternalTransitions = new ArrayList<NoiseEvent>(description.getNoiseDescription().getInternalTransitions().size());
                    for (Transition transition : description.getNoiseDescription().getInternalTransitions())
                    {
                        timedInternalTransitions.add(new NoiseEvent(transition, description.getTime().get(transition)));
                    }
                    description.getNoiseDescription().setExistingNoiseEvents(timedInternalTransitions);
                    LogGeneratorConnection connection = new LogGeneratorConnection("Log generation for " + petrinet,
                            petrinet,
                            description);
                    context.addConnection(connection);
                    GenerationHelper generationHelper = TimeDrivenGenerationHelper.createInstance(petrinet, initialMarking, finalMarking, description);
                    return new Object[]{generate(generationHelper, description), initialMarking, finalMarking, description};

            }
        }
    }

    private TaskListener.InteractionResult configureStartDate()
    {
        JCalendar calendar = new JCalendar(description.getGenerationStart());
        TaskListener.InteractionResult result = context.showWizard("Generation start", false, false, calendar);
        description.setGenerationStart(calendar.getCalendar());
        return result;
    }

    private TaskListener.InteractionResult setGenerationSettings()
    {
        GeneralPropertiesPanel basicProperties = new ExtendedPropertiesPanel(description);
        while (true)
        {
            TaskListener.InteractionResult interactionResult = context.showWizard("General settings", true, false, basicProperties);
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
        ProMJGraphPanel proMJGraphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, petrinet);
        ProMJGraph proMJGraph = proMJGraphPanel.getGraph();
        GraphSettingsListener graphSettingsListener = new GraphSettingsListener(description, proMJGraph);
        proMJGraph.addGraphSelectionListener(graphSettingsListener);
        return context.showWizard("The net given", false, false, proMJGraphPanel);
    }

    private TaskListener.InteractionResult configureNoise(boolean isLast)  //TODO проблема с графикой, если выбрать только пропускание элементов
    {
        //TODO отрефакторить момент с определением последнего окошка? а надо ли вообще?
        TimeDrivenGenerationDescription.NoiseDescription noiseDescription = description.getNoiseDescription();
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
                        result = configureArtificialNoiseTransitions(isLast && !noiseDescription.isUsingTimeGranularity());
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
                    if (noiseDescription.isUsingTimeGranularity())
                    {
                        result = configureTimeGranularity(isLast);
                    }
                    break;
                case 4:
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

    private TaskListener.InteractionResult configureTimeGranularity(boolean isLast)
    {
        SingleChoicePanel<GranularityTypes> granularityChoicePanel = new SingleChoicePanel<GranularityTypes>(GranularityTypes.values(), description.getNoiseDescription().getGranularityType());
        while (true)
        {
            TaskListener.InteractionResult result = context.showWizard("Granularity type", false, isLast, granularityChoicePanel);
            switch (result)
            {
                case CANCEL:
                    return result;
                default:
                    GranularityTypes chosenType = granularityChoicePanel.getChosenOption();
                    if (chosenType == null)
                    {
                        JOptionPane.showMessageDialog(null, "You have to choose granularity type", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        description.getNoiseDescription().setGranularityType(chosenType);
                        return result;
                    }
                    break;
            }
        }
    }

    private TaskListener.InteractionResult setNoiseSettings()
    {
        while (true)
        {
            NoiseSettingsPanel panel = new ExtendedNoiseSettingsPanel(description);
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

    private TaskListener.InteractionResult configureExistentNoiseTransitions(boolean isLast)
    {
        MultipleChoicePanel<Transition> panel;
        if (description.getNoiseDescription().getInternalTransitions() == null)
        {
            panel = new MultipleChoicePanel<Transition>(petrinet.getTransitions());
        }
        else
        {
            panel = new MultipleChoicePanel<Transition>(petrinet.getTransitions(), description.getNoiseDescription().getInternalTransitions());
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

    private TaskListener.InteractionResult configureResources()
    {
        TaskListener.InteractionResult result = TaskListener.InteractionResult.NEXT;
        while (true)
        {
            switch (resourceScreenIndex)
            {
                case -1:
                    if (result == TaskListener.InteractionResult.PREV)
                    {
                        return TaskListener.InteractionResult.PREV;
                    }
                    else
                    {
                        result = TaskListener.InteractionResult.NEXT;
                    }
                    break;
                case 0:
                    if (description.isUsingComplexResourceSettings())
                    {
                        result = createGroups();
                    }
                    break;
                case 1:
                    if (description.isUsingComplexResourceSettings())
                    {
                        result = createRoles();
                    }
                    break;
                case 2:
                    result = createResources();
                    break;
                case 3:
                    result = performResourceMapping();
                    break;
                case 4:
                    if (result == TaskListener.InteractionResult.NEXT)
                    {
                        return TaskListener.InteractionResult.NEXT;
                    }
                    else
                    {
                        result = TaskListener.InteractionResult.PREV;
                    }
                    break;
            }

            switch (result)
            {
                case NEXT:
                    resourceScreenIndex++;
                    break;
                case PREV:
                    resourceScreenIndex--;
                    break;
                default:
                    return result;
            }
        }
    }


    private TaskListener.InteractionResult performResourceMapping()
    {
        JPanel panel;
        boolean canContinue = false;
        TaskListener.InteractionResult result = TaskListener.InteractionResult.CANCEL;
        while (!canContinue)
        {
            canContinue = true;
            if (description.isUsingComplexResourceSettings())
            {
                panel = new ResourcesMappingPanel(petrinet, description);
            }
            else
            {
                panel = new SimplifiedResourceMappingPanel(petrinet, description);
            }
            result = context.showWizard("Resources mapping", false, true, panel);
            if (result == TaskListener.InteractionResult.CANCEL || result == TaskListener.InteractionResult.PREV)
            {
                return result;
            }
            Map<Object, ResourceMapping> mapping = description.getResourceMapping();
            for (ResourceMapping resourceMapping : mapping.values())
            {
                if (description.isUsingComplexResourceSettings())
                {
                    int numberOfResources = resourceMapping.getSelectedResources().size();
                    if (numberOfResources == 0)
                    {
                        canContinue = false;
                    }
                }
                else
                {
                    int numberOfResources = resourceMapping.getSelectedSimplifiedResources().size();
                    if (numberOfResources == 0)
                    {
                        canContinue = false;
                    }
                }
            }
            if (!canContinue)
            {
                JOptionPane.showMessageDialog(null, "You have to choose at least one resource for every transition", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }

    private TaskListener.InteractionResult createGroups()
    {
        List<Group> groups = description.getResourceGroups();
        while (true)
        {
            GroupPanel panel = new GroupPanel(groups);
            TaskListener.InteractionResult result = context.showWizard("Create groups", false, false, panel);
            for (ResourceMapping mapping : description.getResourceMapping().values())
            {
                mapping.retainSelectedGroups(groups);
            }
            if (result == TaskListener.InteractionResult.CANCEL || result == TaskListener.InteractionResult.PREV || groups.size() != 0)
            {
                return result;
            }
            else
            {
                JOptionPane.showMessageDialog(null, "At least one group has to be created", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private TaskListener.InteractionResult createRoles()
    {
        while (true)
        {
            TaskListener.InteractionResult interactionResult = TaskListener.InteractionResult.CANCEL;
            List<Group> groups = description.getResourceGroups();
            indexOfGroupWithinRoleCreation = indexOfGroupWithinRoleCreation == -1 ? 0 : indexOfGroupWithinRoleCreation;
            indexOfGroupWithinRoleCreation = indexOfGroupWithinRoleCreation == groups.size() ? groups.size() - 1 : indexOfGroupWithinRoleCreation;
            while (indexOfGroupWithinRoleCreation >= 0 && indexOfGroupWithinRoleCreation < groups.size())
            {
                RolePanel panel = new RolePanel(groups.get(indexOfGroupWithinRoleCreation));
                interactionResult = context.showWizard("Create roles for " + groups.get(indexOfGroupWithinRoleCreation),
                        false, false, panel);
                switch (interactionResult)
                {
                    case PREV:
                        indexOfGroupWithinRoleCreation--;
                        break;
                    case NEXT:
                        indexOfGroupWithinRoleCreation++;
                        break;
                    case CANCEL:
                        return TaskListener.InteractionResult.CANCEL;
                }
            }
            List<Role> allRoles = new ArrayList<Role>();
            for (Group group : groups)
            {
                allRoles.addAll(group.getRoles());
            }
            for (ResourceMapping mapping : description.getResourceMapping().values())
            {
                mapping.retainSelectedRoles(allRoles);
            }
            if (interactionResult == TaskListener.InteractionResult.CANCEL || interactionResult == TaskListener.InteractionResult.PREV || allRoles.size() != 0)
            {
                return interactionResult;
            }
            else
            {
                JOptionPane.showMessageDialog(null, "At least one role has to be created", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private TaskListener.InteractionResult createResources()
    {
        List<Group> groups = description.getResourceGroups();
        List<Resource> resources = new ArrayList<Resource>();
        for (Group group : groups)
        {
            resources.addAll(group.getResources());
        }
        JPanel panel;
        if (description.isUsingComplexResourceSettings())
        {
            panel = new ResourcePanel(groups, resources, description.isUsingSynchronizationOnResources());

        }
        else
        {
            resources.addAll(description.getSimplifiedResources());
            panel = new SimplifiedResourcePanel(description.getSimplifiedResources(), resources, description.isUsingSynchronizationOnResources());
        }

        TaskListener.InteractionResult result = context.showWizard("Create resources", false, false, panel);
        List<Resource> allResources = new ArrayList<Resource>(description.getSimplifiedResources());
        for (Group group : groups)
        {
            allResources.addAll(group.getResources());
        }
        for (ResourceMapping mapping : description.getResourceMapping().values())
        {
            mapping.retainSelectedResources(allResources);
        }
        return result;
    }

}

