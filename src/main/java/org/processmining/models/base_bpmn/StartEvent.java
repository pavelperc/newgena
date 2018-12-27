package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;

import java.util.Collection;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public class StartEvent extends Event
{
    private final Collection<? extends SequenceFlow> outputFlows;
    private boolean isAvailable = true;

    protected StartEvent(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent, Collection<? extends SequenceFlow> outputFlows)
    {
        super(actualEvent);
        this.outputFlows = outputFlows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MovementResult move(XTrace trace)
    {
        isAvailable = false;

        MovementResult movementResult = new MovementResult(false);

        for (SequenceFlow outputFlow : outputFlows)
        {
            if (!outputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputFlow);
            }

            outputFlow.addToken(createToken());
        }
        return movementResult;
    }

    protected Token createToken()
    {
        return new Token();
    }

    @Override
    public boolean checkAvailability()
    {
        return isAvailable;
    }

    public void moveToInitialState()
    {
        isAvailable = true;
    }


    public static class StartEventBuilder extends AbstractStartEventBuilder<StartEvent, SimpleSequenceFlow, SimpleMessageFlow, SubProcess>
    {
        private final org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent;

        public StartEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent)
        {
            super(actualEvent);
            if (actualEvent == null)
            {
                throw new NullPointerException("Event cannot be equal to null");
            }

            if (actualEvent.getEventType() != org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType.START)
            {
                throw new IllegalArgumentException("Incorrect event type");
            }

            this.actualEvent = actualEvent;
        }

        @Override
        public StartEvent build()
        {
            return new StartEvent(actualEvent, outputSequenceFlows);
        }

    }
}
