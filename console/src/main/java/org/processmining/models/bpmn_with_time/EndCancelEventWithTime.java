package org.processmining.models.bpmn_with_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.base_bpmn.AbstractEndEventBuilder;
import org.processmining.models.base_bpmn.EndCancelEvent;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;

import java.util.List;

/**
 * Created by Ivan on 16.09.2015.
 */
public class EndCancelEventWithTime extends EndCancelEvent implements MovableWithTime
{
    protected EndCancelEventWithTime(Event actualEvent, List<SequenceFlowWithTime> inputFlows, SubProcessWithTime parentSubProcess)
    {
        super(actualEvent, inputFlows, parentSubProcess);
    }

    @Override
    public Long getTimestamp()
    {
        long earliestTimestamp = Long.MAX_VALUE;

        for (SequenceFlowWithTime inFlow : getInputFlows())
        {
            if (inFlow.hasTokens())
            {
                long timestamp = inFlow.peekToken().getTimestamp();

                if (timestamp < earliestTimestamp)
                {
                    earliestTimestamp = timestamp;
                }
            }
        }

        if (earliestTimestamp == Long.MAX_VALUE)
        {
            return null;
        }
        else
        {
            return earliestTimestamp;
        }
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        return cancelParentSubProcess();
    }

    protected MovementResult cancelParentSubProcess()
    {
        return ((SubProcessWithTime)getParentSubProcess()).cancelSubProcess(getTimestamp());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<SequenceFlowWithTime> getInputFlows()
    {
        return (List<SequenceFlowWithTime>) super.getInputFlows();
    }


    public static class EndCancelEventWithTimeBuilder extends AbstractEndEventBuilder<EndCancelEventWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime>
    {

        public EndCancelEventWithTimeBuilder(Event actualNode)
        {
            super(actualNode);
        }

        @Override
        public EndCancelEventWithTime build()
        {
            return new EndCancelEventWithTime(actualNode, inputSequenceFlows, (SubProcessWithTime) parentSubProcess);
        }

    }
}
