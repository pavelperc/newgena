package org.processmining.models.bpmn_with_time;

import org.processmining.models.TokenWithTime;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.AbstractStartEventBuilder;
import org.processmining.models.base_bpmn.StartEvent;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;

import java.util.Collection;

/**
 * Created by Ivan on 10.09.2015.
 */
public class StartEventWithTime extends StartEvent
{
    private long initialTimestamp;               //TODO initialize!

    private StartEventWithTime(Event actualEvent, Collection<? extends SequenceFlowWithTime> outputFlows, long initialTimestamp)
    {
        super(actualEvent, outputFlows);
        this.initialTimestamp = initialTimestamp;
    }

    @Override
    protected Token createToken()
    {
        return new TokenWithTime(initialTimestamp);
    }

    public static class StartEventWithTimeBuilder extends AbstractStartEventBuilder<StartEventWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime>
    {
        private long initialTimestamp;

        public StartEventWithTimeBuilder(Event actualEvent, long initialTimestamp)
        {
            super(actualEvent);
            this.initialTimestamp = initialTimestamp;
        }

        @Override
        public StartEventWithTime build()
        {
            return new StartEventWithTime(actualNode, outputSequenceFlows, initialTimestamp);
        }
    }
}
