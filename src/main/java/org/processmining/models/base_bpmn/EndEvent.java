package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public class EndEvent extends Event
{
    private static final Random random = new Random();
    private final Collection<? extends SequenceFlow> inputFlows;
    private final AbstractSubProcess parentSubProcess;

    protected EndEvent(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent, AbstractSubProcess parentSubProcess, Collection<? extends SequenceFlow> inputFlows)
    {
        super(actualEvent);
        this.parentSubProcess = parentSubProcess;
        this.inputFlows = inputFlows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MovementResult move(XTrace trace)
    {
        MovementResult movementResult = new MovementResult(false);

        for (SequenceFlow inputFlow : inputFlows)
        {
            if (inputFlow.hasTokens())
            {
                inputFlow.removeAllTokens();

                if (!inputFlow.hasTokens())
                {
                    movementResult.addEmptiedTokenable(inputFlow);
                }
            }
        }

        updateParentSubProcess(movementResult);

        return movementResult;
    }

    protected void updateParentSubProcess(MovementResult movementResult)
    {
        if (parentSubProcess != null)
        {
            MovementResult subProcessMovementResult = parentSubProcess.updateState(true);
            movementResult.addAllEmptiedTokenables(subProcessMovementResult.getEmptiedTokenables());
            movementResult.addAllFilledTokenables(subProcessMovementResult.getFilledTokenables());
        }
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

    protected Collection<? extends SequenceFlow> getInputFlows()
    {
        return inputFlows;
    }

    protected AbstractSubProcess getParentSubProcess()
    {
        return parentSubProcess;
    }

    protected <T extends SequenceFlow> T selectFlowToMoveFrom(List<T> inputFlowsWithTokens)
    {
        int index = random.nextInt(inputFlowsWithTokens.size());
        return inputFlowsWithTokens.get(index);
    }

    public static class EndEventBuilder extends AbstractEndEventBuilder<EndEvent, SimpleSequenceFlow, SimpleMessageFlow, SubProcess>
    {
        public EndEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent)
        {
            super(actualEvent);

            if (actualEvent == null)
            {
                throw new NullPointerException("Event cannot be equal to null");
            }

            if (actualEvent.getEventType() != org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType.END)
            {
                throw new IllegalArgumentException("Incorrect event type");
            }
        }

        public EndEvent build()
        {
            return new EndEvent(actualNode, parentSubProcess, inputSequenceFlows);
        }
    }
}
