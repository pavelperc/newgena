package org.processmining.utils.helpers;

import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.AbstractSubProcessBuilder;
import org.processmining.models.bpmn_with_time.*;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.time.managers.ExecutionTimeManager;
import org.processmining.models.time.managers.ScriptExecutionTimeManager;
import org.processmining.models.time.managers.UniformExecutionTimeManager;
import org.processmining.utils.distribution.ConfiguredLongDistribution;

/**
 * Created by Ivan on 31.08.2015.
 */
public class BPMNWithTimeHelperInitializer extends AbstractBPMNHelperInitializer<TokenWithTime, SequenceFlowWithTime,
        MessageFlowWithTime, MovableWithTime>
{
    private BPMNWithTimeGenerationDescription description;

    public BPMNWithTimeHelperInitializer(BPMNDiagram diagram, BPMNWithTimeGenerationDescription description)
    {
        super(diagram);
        this.description = description;
    }

    public BPMNWithTimeGenerationDescription getDescription()
    {
        return description;
    }


    @Override
    protected AbstractSubProcessBuilder<TokenWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime>
    createSubProcessBuilder(org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess)
    {
        return new SubProcessWithTime.SubProcessWithTimeBuilder(subProcess);
    }

    @Override
    protected MessageFlowWithTime createMessageFlow(org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow flow)
    {
        return new MessageFlowWithTime(flow);
    }

    @Override
    protected EndCancelEventWithTime.EndCancelEventWithTimeBuilder createEndCancelEventBuilder(Event event)
    {
        return new EndCancelEventWithTime.EndCancelEventWithTimeBuilder(event);
    }

    @Override
    protected EndEventWithTime.EndEventWithTimeBuilder createEndEventBuilder(Event event)
    {
        return new EndEventWithTime.EndEventWithTimeBuilder(event);
    }

    @Override
    protected StartEventWithTime.StartEventWithTimeBuilder createStartEventBuilder(Event event)
    {
        long startTime = description.getGenerationStart().getTime().getTime();
        return new StartEventWithTime.StartEventWithTimeBuilder(event, startTime);
    }

    @Override
    protected void findGateways()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.Gateway gateway : getDiagram().getGateways())
        {
            GatewayWithTimeBuilder gatewayBuilder = new GatewayWithTimeBuilder(gateway);
            getNodesToConnectivityElementBuilders().put(gateway.getId(), gatewayBuilder);
        }
    }

    @Override
    protected void findActivities()
    {
        boolean separatingStartAndFinishEvents = description.isSeparatingStartAndFinish();

        for (Activity activity : getDiagram().getActivities())
        {
            ExecutionTimeManager<Activity> executionTimeManager;

            if (description.isUsingTimeScript(activity))
            {
                executionTimeManager = new ScriptExecutionTimeManager<>(description.getTimeScriptPath(activity));
            }
            else
            {
                ConfiguredLongDistribution executionTimeDistribution = description.getExecutionTimeDistribution(activity);
                executionTimeManager = new UniformExecutionTimeManager<>(executionTimeDistribution);
            }

            ActivityWithTime.ActivityWithTimeBuilder activityBuilder =
                    new ActivityWithTime.ActivityWithTimeBuilder(activity, separatingStartAndFinishEvents, executionTimeManager,
                            description, description.getTimeScriptPath(activity));
            getNodesToActualMovableBuilders().put(activity.getId(), activityBuilder);
        }
    }

    @Override
    protected SequenceFlowWithTime createSequenceFlow(Flow flow)
    {
        return new SequenceFlowWithTime(flow);
    }
}
