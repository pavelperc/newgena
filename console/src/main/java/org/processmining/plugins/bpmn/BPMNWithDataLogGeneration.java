package org.processmining.plugins.bpmn;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ScriptSelection;
import org.processmining.dialogs.SelectingDataObjectsWithInitialScriptPanel;
import org.processmining.dialogs.Verifiable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.BPMNWithDataGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.DataObject;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.utils.BPMNWithDataLoggingSingleton;
import org.processmining.utils.helpers.BPMNWithDataGenerationHelper;

import javax.swing.*;
import java.util.Set;

/**
 * Created by Ivan on 08.04.2015.
 */
public class BPMNWithDataLogGeneration extends BaseBPMNGenerationPlugin
{
    private BPMNWithDataGenerationDescription description;
    private UIPluginContext context;

    @Plugin
            (
                    name = "GENA: BPMN with data log generator",
                    returnLabels = {"Event log array"},
                    returnTypes = {EventLogArray.class},
                    parameterLabels = "BPMN diagram"
            )
    @UITopiaVariant
            (
                    /*affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov"  */
                    affiliation = "", email = "", author = "")
    public EventLogArray generate(UIPluginContext context, BPMNDiagram diagram)
    {
        this.context = context;
        setContext(context);
        description = new BPMNWithDataGenerationDescription(diagram);

        boolean gatewayDataRulesAreAbsent = false;

        BPMNWithDataLoggingSingleton.init(description, description.isUsingResources());

        int screenNumber = 0;
        TaskListener.InteractionResult result = TaskListener.InteractionResult.CANCEL;
        description.initDataAssociations();

        while (true)
        {
            switch (screenNumber)
            {
                case 0:
                    result = setGenerationSettings(false);
                    break;
                case 1:
                    result = visualizeDiagram(diagram, false);
                    break;
                case 2:
                    result = configureTypesOfDataObjects(diagram);
                    break;
                case 3:
                    if (!description.getDataObjectsWithScripts().isEmpty())
                    {
                        result = setInitialValuesOfDataObjects();
                    }
                    break;
                case 4:
                    result = configureActivityDataRules();
                    break;
                case 5:
                    result = configureGatewayDataRules();
                    break;
            }

            switch (result)
            {
                case NEXT:
                    screenNumber++;
                    break;
                case PREV:
                    screenNumber--;
                    break;
                case CANCEL:
                    return null;
                default:
                    description.setDataObjectScriptPaths();
                    BPMNWithDataGenerationHelper helper = BPMNWithDataGenerationHelper.createHelperWithData(diagram, description);
                    return generate(helper);
            }
        }
    }

    private TaskListener.InteractionResult setInitialValuesOfDataObjects()
    {
        ScriptSelection<DataObject> panel = new ScriptSelection<>(description.getDataObjectToScriptPaths());

        return showVerifiablePanel(panel, "Scripts for data objects", "Some paths are incorrect", false);
    }

    private TaskListener.InteractionResult configureTypesOfDataObjects(BPMNDiagram diagram)
    {
        SelectingDataObjectsWithInitialScriptPanel panel = new SelectingDataObjectsWithInitialScriptPanel(
                diagram.getDataObjects(), description.getDataObjectsWithScripts());
        TaskListener.InteractionResult result = context.showWizard("Data objects initialized with scripts", false, false, panel);

        Set<DataObject> dataObjectsWithScripts = panel.getChosenOptionsAsSet();
        description.setDataObjectsWithScripts(dataObjectsWithScripts);

        return result;
    }

    private TaskListener.InteractionResult configureActivityDataRules()
    {
        ScriptSelection<Activity> panel = new ScriptSelection<>(description.getActivitiesToScriptPaths());

        return showVerifiablePanel(panel, "Scripts for activities and sub-processes", "Some paths are incorrect", false);
    }

    private TaskListener.InteractionResult configureGatewayDataRules()
    {
        ScriptSelection<Gateway> panel = new ScriptSelection<>(description.getGatewaysToScriptPaths());

        return showVerifiablePanel(panel, "Scripts for gateways", "Some paths are incorrect", true);
    }

    @Override
    protected GenerationDescription getGenerationDescription()
    {
        return description;
    }

    private TaskListener.InteractionResult showVerifiablePanel(Verifiable panel, String title, String errorMessage, boolean last)
    {
        boolean verified;
        TaskListener.InteractionResult result;
        boolean repeatLoop;

        JPanel jPanel = (JPanel) panel;

        do
        {
            result = context.showWizard(title, false, last, jPanel);

            verified = panel.verify();

            repeatLoop = !verified && result != TaskListener.InteractionResult.CANCEL && result != TaskListener.InteractionResult.PREV;

            if (repeatLoop)
            {
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } while (repeatLoop);

        return result;
    }
}
