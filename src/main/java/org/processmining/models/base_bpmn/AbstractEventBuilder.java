package org.processmining.models.base_bpmn;

import org.processmining.models.Movable;

/**
 * Created by Ivan on 21.09.2015.
 */
public abstract class AbstractEventBuilder<M extends Movable, SF extends SequenceFlow, MF extends MessageFlow>
        extends AbstractNodeBuilder<M, org.processmining.models.graphbased.directed.bpmn.elements.Event, SF, MF>
{
    protected AbstractEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent)
    {
        super(actualEvent);
    }

    @Override
    public void incomingMessageFlow(MF inputFlow)
    {
        throw new IllegalStateException("Events do not support incoming message flows");
    }

    @Override
    public void outgoingMessageFlow(MF outputFlow)
    {
        throw new IllegalStateException("Events do not support outgoing message flows");
    }
}
