package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.Movable;
import org.processmining.models.MovementResult;

import java.util.List;

/**
 * Created by Ivan on 05.03.2015.
 */
public class EndCancelEvent extends Event implements Movable
{
    private final List<? extends SequenceFlow> inputFlows;
    private final AbstractSubProcess parentSubProcess;

    protected EndCancelEvent(
            org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent,
            List<? extends SequenceFlow> inputFlows, AbstractSubProcess parentSubProcess)
    {
        super(actualEvent);
        this.inputFlows = inputFlows;
        this.parentSubProcess = parentSubProcess;
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        return cancelParentSubProcess();
    }

    protected MovementResult cancelParentSubProcess()
    {
        return parentSubProcess.cancelSubProcess();
    }

    @Override
    public boolean checkAvailability()
    {

        for (SequenceFlow inputFlow : inputFlows)
        {
            if (inputFlow.hasTokens())
            {
                return true;
            }
        }

        return false;
    }

    protected List<? extends SequenceFlow> getInputFlows()
    {
        return inputFlows;
    }

    protected AbstractSubProcess getParentSubProcess()
    {
        return parentSubProcess;
    }

    public static class EndCancelEventBuilder extends AbstractEndEventBuilder<EndCancelEvent, SimpleSequenceFlow, SimpleMessageFlow, SubProcess>
    {

        public EndCancelEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event actualNode)
        {
            super(actualNode);
        }

        @Override
        public EndCancelEvent build()
        {
            return new EndCancelEvent(actualNode, inputSequenceFlows, parentSubProcess);
        }

    }
}
