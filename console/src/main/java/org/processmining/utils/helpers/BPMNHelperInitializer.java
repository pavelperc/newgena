package org.processmining.utils.helpers;

import org.processmining.models.Movable;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;

/**
 * Created by Ivan Shugurov on 11.02.2015.
 */
public class BPMNHelperInitializer extends AbstractBPMNHelperInitializer<Token, SimpleSequenceFlow, SimpleMessageFlow, Movable>
{
    //TODO делать публичным, когда всё пофикшу?
    protected BPMNHelperInitializer(BPMNDiagram diagram)
    {
        super(diagram);
    }

    public static BPMNHelperInitializer createSimpleBPMNHelperInitializer(BPMNDiagram diagram)
    {
        return new BPMNHelperInitializer(diagram);
    }

    @Override
    protected SimpleMessageFlow createMessageFlow(org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow flow)
    {
        return new SimpleMessageFlow(flow);
    }

    @Override
    protected AbstractSubProcessBuilder createSubProcessBuilder(org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess)
    {
        return new SubProcess.SubProcessBuilder(subProcess);
    }

    @Override
    protected AbstractEndEventBuilder<? extends org.processmining.models.base_bpmn.Event, SimpleSequenceFlow, SimpleMessageFlow, SubProcess> createEndCancelEventBuilder(Event event)
    {
        return new EndCancelEvent.EndCancelEventBuilder(event);
    }


    @Override
    protected AbstractEndEventBuilder<? extends org.processmining.models.base_bpmn.Event, SimpleSequenceFlow, SimpleMessageFlow, SubProcess> createEndEventBuilder(Event event)
    {
        return new EndEvent.EndEventBuilder(event);
    }

    @Override
    protected AbstractStartEventBuilder<? extends org.processmining.models.base_bpmn.Event, SimpleSequenceFlow, SimpleMessageFlow, SubProcess> createStartEventBuilder(Event event)
    {
        return new StartEvent.StartEventBuilder(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void findGateways()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.Gateway gateway : getDiagram().getGateways())
        {
            AbstractNodeBuilder<Movable, BPMNNode, SimpleSequenceFlow, SimpleMessageFlow> gatewayBuilder =
                    (AbstractNodeBuilder) new Gateway.GatewayBuilder(gateway);
            getNodesToConnectivityElementBuilders().put(gateway.getId(), gatewayBuilder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void findActivities()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.Activity activity : getDiagram().getActivities())
        {
            org.processmining.models.base_bpmn.Activity.ActivityBuilder activityBuilder = new org.processmining.models.base_bpmn.Activity.ActivityBuilder(activity);
            getNodesToActualMovableBuilders().put(activity.getId(), (AbstractNodeBuilder) activityBuilder);
        }

        for (org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess : getDiagram().getSubProcesses())
        {
            if (subProcess.isBCollapsed())
            {
                org.processmining.models.base_bpmn.Activity.ActivityBuilder activityBuilder = new org.processmining.models.base_bpmn.Activity.ActivityBuilder(subProcess);
                getNodesToActualMovableBuilders().put(subProcess.getId(), (AbstractNodeBuilder) activityBuilder);
            }
        }
    }


    @Override
    protected SimpleSequenceFlow createSequenceFlow(Flow flow)
    {
        return new SimpleSequenceFlow(flow);
    }


}
