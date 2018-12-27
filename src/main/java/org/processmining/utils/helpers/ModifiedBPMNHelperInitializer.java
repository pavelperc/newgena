package org.processmining.utils.helpers;

import org.processmining.models.Movable;
import org.processmining.models.base_bpmn.AbstractNodeBuilder;
import org.processmining.models.base_bpmn.SimpleMessageFlow;
import org.processmining.models.base_bpmn.SimpleSequenceFlow;
import org.processmining.models.descriptions.ModifiedBPMNGenerationDescription;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.modified_bpmn.ExclusiveGatewayWithPreferences;

import java.util.Map;

/**
 * Created by Ivan Shugurov on 11.02.2015.
 */
public class ModifiedBPMNHelperInitializer extends BPMNHelperInitializer
{
    private final ModifiedBPMNGenerationDescription description;

    public ModifiedBPMNHelperInitializer(BPMNDiagram diagram, ModifiedBPMNGenerationDescription description)
    {
        super(diagram);
        this.description = description;
    }

    @Override
    protected void findGateways()
    {
        BPMNDiagram diagram = getDiagram();
        Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SimpleSequenceFlow, SimpleMessageFlow>>
                nodesToConnectivityElementBuilders = getNodesToConnectivityElementBuilders();

        for (Gateway gateway : diagram.getGateways())
        {
            if (description.contains(gateway))
            {
                AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SimpleSequenceFlow, SimpleMessageFlow> gatewayBuilder =
                        new ExclusiveGatewayWithPreferences.ExclusiveGatewayWithPreferencesBuilder(gateway);
                nodesToConnectivityElementBuilders.put(gateway.getId(), gatewayBuilder);
            }
            else
            {
                org.processmining.models.base_bpmn.Gateway.GatewayBuilder gatewayBuilder =
                        new org.processmining.models.base_bpmn.Gateway.GatewayBuilder(gateway);

                nodesToConnectivityElementBuilders.put(gateway.getId(), gatewayBuilder);
            }
        }
    }

    @Override
    protected void addOutputFlow(SimpleSequenceFlow sequenceFlow, BPMNNode source)
    {
        if (source instanceof Gateway)
        {
            Gateway gateway = (Gateway) source;

            if (description.contains(gateway))
            {
                Map<Flow, Integer> preferences = description.getPreferences(gateway);

                Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SimpleSequenceFlow, SimpleMessageFlow>>
                        nodesToConnectivityElementBuilders = getNodesToConnectivityElementBuilders();

                ExclusiveGatewayWithPreferences.ExclusiveGatewayWithPreferencesBuilder builder =
                        (ExclusiveGatewayWithPreferences.ExclusiveGatewayWithPreferencesBuilder) (AbstractNodeBuilder)nodesToConnectivityElementBuilders.get(source.getId());


                int preference = preferences.get(sequenceFlow.getFlow());
                builder.outputFlow(sequenceFlow, preference);
            }
            else
            {
                super.addOutputFlow(sequenceFlow, source);
            }
        }
        else
        {
            super.addOutputFlow(sequenceFlow, source);
        }
    }

}
