package org.processmining.models.base_bpmn;

import org.processmining.models.Movable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public abstract class Gateway implements Movable
{
    protected final static Random random = new Random();
    private final org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway;
    private final List<SequenceFlow> inputSequenceFlows;
    private final List<SequenceFlow> outputSequenceFlows;
    private final AbstractSubProcess parentSubProcess;

    protected Gateway(
            org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway,
            AbstractSubProcess parentSubProcess,
            List<? extends SequenceFlow> inputSequenceFlows,
            List<? extends SequenceFlow> outputSequenceFlows)
    {
        this.actualGateway = actualGateway;
        this.parentSubProcess = parentSubProcess;
        this.inputSequenceFlows = Collections.unmodifiableList(inputSequenceFlows);
        this.outputSequenceFlows = Collections.unmodifiableList((outputSequenceFlows));
    }

    public List<? extends SequenceFlow> getOutputSequenceFlows()
    {
        return outputSequenceFlows;
    }

    public List<? extends SequenceFlow> getInputSequenceFlows()
    {
        return inputSequenceFlows;
    }

    public org.processmining.models.graphbased.directed.bpmn.elements.Gateway getActualGateway()
    {
        return actualGateway;
    }

    @Override
    public String toString()
    {
        return "Loggable version of " + actualGateway;
    }

    public AbstractSubProcess getParentSubProcess()
    {
        return parentSubProcess;
    }

    public static class GatewayBuilder extends AbstractNodeBuilder<Gateway, org.processmining.models.graphbased.directed.bpmn.elements.Gateway, SimpleSequenceFlow, SimpleMessageFlow>
    {
        public GatewayBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway)
        {
            super(actualGateway);
        }

        @Override
        public Gateway build()
        {
            if (actualNode == null)
            {
                return new ParallelGateway(null, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
            }

            Gateway gateway;

            switch (actualNode.getGatewayType())
            {
                case PARALLEL:
                    gateway = new ParallelGateway(actualNode, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
                    break;
                case DATABASED:
                    gateway = new ExclusiveChoiceGateway(actualNode, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
                    break;
                default:
                    throw new IllegalStateException("Unknown gateway type");
            }

            return gateway;
        }

        @Override
        public void incomingMessageFlow(SimpleMessageFlow inputFlow)
        {
            throw new IllegalStateException("Message flows are not allowed for gateways");
        }

        @Override
        public void outgoingMessageFlow(SimpleMessageFlow outputFlow)
        {
            throw new IllegalStateException("Message flows are not allowed for gateways");
        }
    }
}
