package org.processmining.models.base_bpmn;

import org.processmining.models.Movable;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public abstract class AbstractNodeBuilder<T extends Movable, K extends BPMNNode, S extends SequenceFlow, M extends MessageFlow>
{
    protected final List<S> inputSequenceFlows = new ArrayList<>();
    protected final List<S> outputSequenceFlows = new ArrayList<>();
    protected final List<M> inputMessageFlows = new ArrayList<>();
    protected final List<M> outputMessageFlows = new ArrayList<>();
    protected final K actualNode;
    protected AbstractSubProcess parentSubProcess;

    protected AbstractNodeBuilder(K actualNode)
    {
        this.actualNode = actualNode;
    }

    public void inputFlow(S flow)
    {
        if (flow == null)
        {
            throw new NullPointerException("Sequence flow cannot be equal to null");
        }
        inputSequenceFlows.add(flow);
    }

    public void outputFlow(S flow)
    {
        if (flow == null)
        {
            throw new NullPointerException("Sequence flow cannot be equal to null");
        }
        outputSequenceFlows.add(flow);
    }

    public void parentSubProcess(AbstractSubProcess parentSubProcess)
    {
        this.parentSubProcess = parentSubProcess;
    }

    public abstract T build();

    public K getActualNode()
    {
        return actualNode;
    }

    public void incomingMessageFlow(M inputFlow)
    {
        checkMessageFlowNotNull(inputFlow);

        inputMessageFlows.add(inputFlow);
    }

    public void outgoingMessageFlow(M outputFlow)
    {
        checkMessageFlowNotNull(outputFlow);

        outputMessageFlows.add(outputFlow);
    }

    private void checkMessageFlowNotNull(M flow)
    {
        if (flow == null)
        {
            throw new NullPointerException("Message flow cannot be equal to null");
        }
    }

}
