package org.processmining.models.base_bpmn;

/**
 * Created by Ivan on 21.09.2015.
 */
public abstract class AbstractStartEventBuilder<M extends StartEvent, SF extends SequenceFlow, MF extends MessageFlow, SP extends AbstractSubProcess> extends
        AbstractEventBuilder<M, SF, MF>
{
    protected AbstractStartEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event actualEvent)
    {
        super(actualEvent);
    }

    @Override
    public void inputFlow(SF flow)
    {
        throw new IllegalStateException("Start events do not support incoming sequence flows;");
    }
}
