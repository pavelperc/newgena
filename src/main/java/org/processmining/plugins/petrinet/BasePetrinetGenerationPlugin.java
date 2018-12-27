package org.processmining.plugins.petrinet;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.GeneratorWrapper;
import org.processmining.utils.helpers.GenerationHelper;
import ru.hse.pais.shugurov.widgets.panels.MultipleChoicePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class BasePetrinetGenerationPlugin
{

    protected Petrinet petrinet;
    protected UIPluginContext context;
    protected Marking initialMarking;
    protected Marking finalMarking;
    private int markingState = 0; //should be used only within setMarking()

    protected TaskListener.InteractionResult chooseMarking(boolean isLast, Marking currentMarking, Marking excludedMarking, String title, String errorMessage)
    {
        List<Place> temp = new ArrayList<Place>(petrinet.getPlaces());
        temp.removeAll(excludedMarking);
        while (true)
        {
            MultipleChoicePanel<Place> markingPanel = new MultipleChoicePanel<Place>(temp, currentMarking);
            TaskListener.InteractionResult result = context.showWizard(title, false, isLast, markingPanel);
            switch (result)
            {
                case PREV:
                case CANCEL:
                    handleMarking(markingPanel.getChosenOptionsAsSet(), currentMarking);
                    return result;
                default:
                    boolean handleResult = handleMarking(markingPanel.getChosenOptionsAsSet(), currentMarking);
                    if (handleResult)
                    {
                        return result;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
            }
        }
    }

    /*returns true if at least one places is chosen, otherwise returns false false*/
    private boolean handleMarking(Collection<Place> chosenPlaces, Marking currentMarking)
    {
        boolean isCorrect = true;
        currentMarking.clear();
        if (chosenPlaces.isEmpty())
        {
            isCorrect = false;
        }
        else
        {
            currentMarking.addAll(chosenPlaces);
        }
        return isCorrect;
    }

    protected TaskListener.InteractionResult setMarking(boolean isLast)
    {
        TaskListener.InteractionResult result = TaskListener.InteractionResult.CANCEL;
        while (true)
        {
            switch (markingState)
            {
                case -1:
                    markingState = 0;
                    return result;
                case 0:  //set initial marking
                    result = chooseMarking(false, initialMarking, finalMarking, "Initial marking - choose initial places", "You have to choose at least one initial place");
                    break;
                case 1:  //set final marking
                    result = chooseMarking(isLast, finalMarking, initialMarking, "Final marking - choose final places", "You have to choose at least one final place");
                    break;
                case 2:
                    markingState = 1;
                    return result;
            }
            switch (result)
            {
                case NEXT:
                    markingState++;
                    break;
                case PREV:
                    markingState--;
                    break;
                default:
                    return result;
            }
        }
    }

    protected void findMarking()
    {
        tryToGetInitialMarking();
        tryToGetFinalMarking();
        initialMarking.minus(finalMarking);
    }

    protected void tryToGetInitialMarking()
    {
        ConnectionManager connectionManager = context.getConnectionManager();
        try
        {
            InitialMarkingConnection initialMarkingConnection = connectionManager.getFirstConnection(InitialMarkingConnection.class, context, petrinet);
            initialMarking = initialMarkingConnection.getObjectWithRole(InitialMarkingConnection.MARKING);
        } catch (ConnectionCannotBeObtained connectionCannotBeObtained)
        {
            connectionCannotBeObtained.printStackTrace();
            initialMarking = new Marking();
            InitialMarkingConnection initialMarkingConnection = new InitialMarkingConnection(petrinet, initialMarking);
            context.addConnection(initialMarkingConnection);
        }
    }

    protected void tryToGetFinalMarking()
    {
        ConnectionManager connectionManager = context.getConnectionManager();
        try
        {
            FinalMarkingConnection finalMarkingConnection = connectionManager.getFirstConnection(FinalMarkingConnection.class, context, petrinet);
            finalMarking = finalMarkingConnection.getObjectWithRole(FinalMarkingConnection.MARKING);
        } catch (ConnectionCannotBeObtained connectionCannotBeObtained)
        {
            connectionCannotBeObtained.printStackTrace();
            finalMarking = new Marking();
            FinalMarkingConnection finalMarkingConnection = new FinalMarkingConnection(petrinet, finalMarking);
            context.addConnection(finalMarkingConnection);
        }
    }

    protected EventLogArray generate(GenerationHelper helper, GenerationDescription description)
    {
        return GeneratorWrapper.generate(helper, description, context);
    }
}
