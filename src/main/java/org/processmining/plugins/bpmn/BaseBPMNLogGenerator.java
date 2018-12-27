package org.processmining.plugins.bpmn;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.BasicBPMNGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.utils.BPMNLoggingSingleton;
import org.processmining.utils.helpers.SimpleBPMNHelper;

/**
 * Created by Ivan Shugurov on 21.12.2014.
 */
public class BaseBPMNLogGenerator extends BaseBPMNGenerationPlugin
{
    private BasicBPMNGenerationDescription description;

    @Plugin
            (
                    name = "GENA: Base BPMN log generator",
                    returnLabels = {"Event log array"},
                    returnTypes = {EventLogArray.class},
                    parameterLabels = "BPMN diagram"
            )
    @UITopiaVariant
            (
                    /*affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov" */
                    affiliation = "", email = "", author = "")
    public EventLogArray generate(UIPluginContext context, BPMNDiagram diagram)
    {
        setContext(context);
        description = new BasicBPMNGenerationDescription(!diagram.getSwimlanes().isEmpty() || !diagram.getPools().isEmpty());

        BPMNLoggingSingleton.init(description.isUsingResources());
        TaskListener.InteractionResult result = setGenerationSettings(true);


        switch (result)
        {
            case CANCEL:
                return null;
            default:
                SimpleBPMNHelper helper = SimpleBPMNHelper.createSimpleHelper(diagram, description);
                return generate(helper);
        }
    }


    @Override
    protected GenerationDescription getGenerationDescription()
    {
        return description;
    }
}
