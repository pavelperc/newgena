package org.processmining.models.base_bpmn;

import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 17.09.2015.
 */
public abstract class AbstractSubProcessBuilder<T extends Token, SF extends SequenceFlow<T>, MF extends MessageFlow<T>, SP extends AbstractSubProcess>
        extends AbstractNodeBuilder<SP, SubProcess, SF, MF>
{
    private List<SF> internalFlows = new ArrayList<>();
    private List<SF> dummyFlowsToFrontSubProcessElements = new ArrayList<>();
    private List<SF> cancelOutputFlows = new ArrayList<>();

    protected AbstractSubProcessBuilder(SubProcess actualNode)
    {
        super(actualNode);
    }

    @Override     //TODO!!!!
    public void outputFlow(SF flow)
    {
        Flow actualFlow = flow.getFlow();

        if (actualFlow.getTarget().getParentSubProcess() == actualNode)
        {
            flowFromStartEvent(flow);
        }
        else
        {
            super.outputFlow(flow);
        }
    }

    public void cancelOutputFlow(SF cancelOutputFlow)
    {
        checkSequenceFlow(cancelOutputFlow);
        cancelOutputFlows.add(cancelOutputFlow);
    }

    public void internalSequenceFlow(SF internalFlow)
    {
        checkSequenceFlow(internalFlow);

        internalFlows.add(internalFlow);
    }

    private void checkSequenceFlow(SF flow)
    {
        if (flow == null)
        {
            throw new NullPointerException("Sequence flow cannot be equal to null");
        }
    }

    protected void flowFromStartEvent(SF flow)
    {
        checkSequenceFlow(flow);
        dummyFlowsToFrontSubProcessElements.add(flow);
    }

    //getters

    protected List<SF> getInternalFlows()
    {
        return internalFlows;
    }

    protected List<SF> getDummyFlowsToFrontSubProcessElements()
    {
        return dummyFlowsToFrontSubProcessElements;
    }

    protected List<SF> getCancelOutputFlows()
    {
        return cancelOutputFlows;
    }
}
