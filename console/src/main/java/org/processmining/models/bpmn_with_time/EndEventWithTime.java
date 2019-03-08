package org.processmining.models.bpmn_with_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.AbstractEndEventBuilder;
import org.processmining.models.base_bpmn.EndEvent;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Ivan on 10.09.2015.
 */
public class EndEventWithTime extends EndEvent implements MovableWithTime
{

    protected EndEventWithTime(Event actualEvent, SubProcessWithTime parentSubProcess, Collection<SequenceFlowWithTime> inputFlows)
    {
        super(actualEvent, parentSubProcess, inputFlows);
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        long earliestTimestamp = Long.MAX_VALUE;
        List<SequenceFlowWithTime> earliestFlows = new ArrayList<>();

        for (SequenceFlowWithTime inFlow : getInputFlows())
        {
            if (inFlow.hasTokens())
            {
                TokenWithTime token = inFlow.peekToken();
                long tokenTimestamp = token.getTimestamp();

                if (earliestTimestamp > tokenTimestamp)
                {
                    earliestTimestamp = tokenTimestamp;
                    earliestFlows.clear();
                    earliestFlows.add(inFlow);
                }
                else
                {
                    if (earliestTimestamp == tokenTimestamp)
                    {
                        earliestFlows.add(inFlow);
                    }
                }
            }
        }

        SequenceFlowWithTime flow = selectFlowToMoveFrom(earliestFlows);

        flow.consumeToken();


        MovementResult movementResult = new MovementResult();

        if (!flow.hasTokens())
        {
            movementResult.addEmptiedTokenable(flow);
        }

        if (getParentSubProcess() != null)
        {
            MovementResult subProcessMovementResult = ((SubProcessWithTime)getParentSubProcess()).updateState(earliestTimestamp);
            movementResult.addAllEmptiedTokenables(subProcessMovementResult.getEmptiedTokenables());
            movementResult.addAllFilledTokenables(subProcessMovementResult.getFilledTokenables());
        }

        return movementResult;
    }


    @Override
    @SuppressWarnings("unchecked")
    protected Collection<SequenceFlowWithTime> getInputFlows()
    {
        return (Collection<SequenceFlowWithTime>) super.getInputFlows();
    }

    @Override
    public Long getTimestamp()
    {
        long timestamp = Long.MAX_VALUE;

        for (SequenceFlowWithTime inFlow : getInputFlows())
        {
            if (inFlow.hasTokens())
            {
                TokenWithTime token = inFlow.peekToken();
                long tokenTimestamp = token.getTimestamp();

                if (tokenTimestamp < timestamp)
                {
                    timestamp = tokenTimestamp;
                }
            }
        }

        if (timestamp == Long.MAX_VALUE)
        {
            return null;
        }
        else
        {
            return timestamp;
        }
    }

    public static class EndEventWithTimeBuilder extends AbstractEndEventBuilder<EndEventWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime>
    {

        public EndEventWithTimeBuilder(Event actualEvent)
        {
            super(actualEvent);
        }

        @Override
        @SuppressWarnings("unchecked")
        public EndEventWithTime build()
        {
            return new EndEventWithTime(getActualNode(), (SubProcessWithTime) parentSubProcess, (Collection) inputSequenceFlows);
        }
    }
}
