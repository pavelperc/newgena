package org.processmining.models.bpmn_with_time;

import org.processmining.models.base_bpmn.AbstractNodeBuilder;
import org.processmining.models.base_bpmn.Gateway;

/**
 * Created by Ivan on 01.09.2015.
 */
public class GatewayWithTimeBuilder extends AbstractNodeBuilder<
        Gateway, org.processmining.models.graphbased.directed.bpmn.elements.Gateway,
        SequenceFlowWithTime, MessageFlowWithTime>
{
    public GatewayWithTimeBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualNode)
    {
        super(actualNode);
    }

    @Override
    public Gateway build()
    {
        if (actualNode == null)
        {
            return new ParallelGatewayWithTime(actualNode, (SubProcessWithTime) parentSubProcess, inputSequenceFlows, outputSequenceFlows);
        }

        Gateway gateway;

        switch (actualNode.getGatewayType())
        {
            case PARALLEL:
                gateway = new ParallelGatewayWithTime(actualNode, (SubProcessWithTime) parentSubProcess, inputSequenceFlows, outputSequenceFlows);
                break;
            case DATABASED:
                gateway = new ExclusiveGatewayWithTime(actualNode, (SubProcessWithTime) parentSubProcess, inputSequenceFlows, outputSequenceFlows);
                break;
            default:
                throw new IllegalStateException("Unknown gateway type");
        }

        return gateway;
    }
}
