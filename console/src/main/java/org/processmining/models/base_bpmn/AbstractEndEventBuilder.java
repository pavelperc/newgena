package org.processmining.models.base_bpmn;

import org.processmining.models.Movable;

/**
 * Created by Ivan on 21.09.2015.
 */
public abstract class AbstractEndEventBuilder<M extends Movable, SF extends SequenceFlow, MF extends MessageFlow, SP extends AbstractSubProcess> extends AbstractEventBuilder<M, SF, MF>
{
    protected AbstractEndEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent)
    {
        super(actualEvent);
    }

    @Override
    public void outputFlow(SF flow)
    {
        throw new IllegalStateException("End events do not support output flows");
    }
}
