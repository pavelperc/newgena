package org.processmining.utils.helpers;

import org.processmining.models.Movable;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.bpmn_with_data.ActivityWithDataObjects;
import org.processmining.models.bpmn_with_data.ExclusiveGatewayWithData;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.bpmn_with_data.SubProcessWithDataObjects;
import org.processmining.models.descriptions.DescriptionWithDataObjects;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;

import java.util.Map;
import java.util.Set;

/**
 * Created by Ivan on 21.04.2015.
 */
public class BPMNWithDataHelperInitializer extends BPMNHelperInitializer
{
    private DescriptionWithDataObjects description;

    public BPMNWithDataHelperInitializer(BPMNDiagram diagram, DescriptionWithDataObjects description)
    {
        super(diagram);
        this.description = description;
    }

    @Override
    protected AbstractSubProcessBuilder createSubProcessBuilder(SubProcess subProcess)
    {
        if (description.getOutputDataObjects(subProcess).isEmpty() && description.getInputDataObjects(subProcess).isEmpty())
        {
            return super.createSubProcessBuilder(subProcess);
        }
        else
        {
            //TODO not to forget about data objects!
            String scriptPath = description.getActivityScriptPath(subProcess);
            SubProcessWithDataObjects.SubProcessWithDataObjectsBuilder builder =
                    new SubProcessWithDataObjects.SubProcessWithDataObjectsBuilder(
                            subProcess, scriptPath);

            for (LoggableStringDataObject inputDataObject : description.getInputDataObjects(subProcess))
            {
                builder.inputDataObject(inputDataObject);
            }

            for (LoggableStringDataObject outgoingDataObject : description.getOutputDataObjects(subProcess))
            {
                builder.outputDataObject(outgoingDataObject);
            }

            return builder;
        }
    }

    @Override
    protected void findGateways()
    {
        Set<org.processmining.models.graphbased.directed.bpmn.elements.Gateway> gatewaysWithInputDataObjects
                = description.getExclusiveGatewaysWithInputDataObjects(); //TODO how does it work in case of 1 outgoing flows?

        Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SimpleSequenceFlow, SimpleMessageFlow>>
                nodesToConnectivityElementBuilders = getNodesToConnectivityElementBuilders();

        for (org.processmining.models.graphbased.directed.bpmn.elements.Gateway gateway : getDiagram().getGateways())
        {
            if (gatewaysWithInputDataObjects.contains(gateway))
            {
                ExclusiveGatewayWithData.ExclusiveGatewayWithDataBuilder builder =
                        new ExclusiveGatewayWithData.ExclusiveGatewayWithDataBuilder(gateway,
                                description.getGatewaysToScriptPaths().get(gateway));

                for (LoggableStringDataObject inputDataObject : description.getInputDataObjects(gateway))
                {
                    builder.inputDataObject(inputDataObject);
                }

                nodesToConnectivityElementBuilders.put(gateway.getId(), builder);
            }
            else
            {
                Gateway.GatewayBuilder gatewayBuilder = new Gateway.GatewayBuilder(gateway);
                nodesToConnectivityElementBuilders.put(gateway.getId(), gatewayBuilder);
            }

        }
    }

    @Override
    protected void findActivities()
    {
        BPMNDiagram diagram = getDiagram();

        for (org.processmining.models.graphbased.directed.bpmn.elements.Activity activity : diagram.getActivities())
        {
            AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SimpleSequenceFlow, SimpleMessageFlow> activityBuilder;

            boolean hasDataAssociations = description.getActivityScriptPath(activity) != null;

            if (hasDataAssociations)
            {
                ActivityWithDataObjects.ActivityWithDataObjectsBuilder builder =
                        new ActivityWithDataObjects.ActivityWithDataObjectsBuilder(activity,
                                description.getActivityScriptPath(activity));

                for (LoggableStringDataObject dataObject : description.getOutputDataObjects(activity))
                {
                    builder.outputDataObject(dataObject);
                }

                for (LoggableStringDataObject dataObject : description.getInputDataObjects(activity))
                {
                    builder.inputDataObject(dataObject);
                }

                activityBuilder = builder;
            }
            else
            {
                activityBuilder = new Activity.ActivityBuilder(activity);
            }

            getNodesToActualMovableBuilders().put(activity.getId(), activityBuilder);
        }
    }

    @Override
    protected void addOutputFlow(SimpleSequenceFlow sequenceFlow, BPMNNode source)
    {
        Set<org.processmining.models.graphbased.directed.bpmn.elements.Gateway> exclusiveGatewaysWithData =
                description.getExclusiveGatewaysWithInputDataObjects();

        if (exclusiveGatewaysWithData.contains(source))
        {
            ExclusiveGatewayWithData.ExclusiveGatewayWithDataBuilder builder =
                    (ExclusiveGatewayWithData.ExclusiveGatewayWithDataBuilder) (AbstractNodeBuilder) getNodesToConnectivityElementBuilders().get(source.getId());

            builder.outputFlow(sequenceFlow);
        }
        else
        {
            super.addOutputFlow(sequenceFlow, source);
        }

    }

}
