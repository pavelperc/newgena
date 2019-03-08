package org.processmining.utils.helpers;

import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.AbstractSubProcessBuilder;
import org.processmining.models.bpmn_data_time.ActivityWithTimeAndData;
import org.processmining.models.bpmn_data_time.GatewayWithTimeAndDataBuilder;
import org.processmining.models.bpmn_data_time.SubProcessWithTimeAndData;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.bpmn_with_time.*;
import org.processmining.models.descriptions.BPMNWithTimeAndDataDescription;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.models.time.managers.ExecutionTimeManager;
import org.processmining.models.time.managers.ScriptExecutionTimeManager;
import org.processmining.models.time.managers.UniformExecutionTimeManager;
import org.processmining.utils.distribution.ConfiguredLongDistribution;

/**
 * Created by Ivan on 24.02.2016.
 */
public class BPMNWithTimeAndDataHelperInitializer extends BPMNWithTimeHelperInitializer
{
    public BPMNWithTimeAndDataHelperInitializer(BPMNDiagram diagram, BPMNWithTimeGenerationDescription description)
    {
        super(diagram, description);
    }

    @Override
    public BPMNWithTimeAndDataDescription getDescription()
    {
        return (BPMNWithTimeAndDataDescription) super.getDescription();
    }

    @Override
    protected AbstractSubProcessBuilder<TokenWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime> createSubProcessBuilder(SubProcess subProcess)
    {
        BPMNWithTimeAndDataDescription description = getDescription();

        if (description.getInputDataObjects(subProcess).isEmpty() && description.getOutputDataObjects(subProcess).isEmpty())
        {
            return new SubProcessWithTime.SubProcessWithTimeBuilder(subProcess);
        }
        else
        {
            SubProcessWithTimeAndData.SubProcessWithTimeAndDataBuilder builder =
                    new SubProcessWithTimeAndData.SubProcessWithTimeAndDataBuilder(subProcess,
                            description.getActivityScriptPath(subProcess));

            for (LoggableStringDataObject input : description.getInputDataObjects(subProcess))
            {
                builder.inputDataObject(input);
            }

            for (LoggableStringDataObject output : description.getOutputDataObjects(subProcess))
            {
                builder.outputDataObject(output);
            }

            return builder;
        }
    }

    @Override
    protected void findGateways()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.Gateway gateway : getDiagram().getGateways())
        {
            String scriptPath = getDescription().getGatewayScriptPath(gateway);

            GatewayWithTimeBuilder gatewayBuilder;

            if (scriptPath == null)
            {
                gatewayBuilder = new GatewayWithTimeBuilder(gateway);
            }
            else
            {
                gatewayBuilder = new GatewayWithTimeAndDataBuilder(gateway, scriptPath);
            }

            getNodesToConnectivityElementBuilders().put(gateway.getId(), gatewayBuilder);
        }
    }

    @Override
    protected void findActivities()
    {
        BPMNWithTimeAndDataDescription description = getDescription();

        boolean separatingStartAndFinishEvents = description.isSeparatingStartAndFinish();

        for (Activity activity : getDiagram().getActivities())
        {
            String scriptPath = description.getActivityScriptPath(activity);

            ActivityWithTime.ActivityWithTimeBuilder activityBuilder;

            ExecutionTimeManager<Activity> timeManager;

            if (description.isUsingTimeScript(activity))
            {
                timeManager = new ScriptExecutionTimeManager<>(scriptPath);
            }
            else
            {
                ConfiguredLongDistribution executionTimeDistribution = description.getExecutionTimeDistribution(activity);
                timeManager = new UniformExecutionTimeManager<>(executionTimeDistribution);
            }

            activityBuilder =
                    new ActivityWithTimeAndData.ActivityWithTimeAndDataBuilder(activity, separatingStartAndFinishEvents,
                            timeManager, scriptPath, description, description.getTimeScriptPath(activity));

            for (LoggableStringDataObject dataObject : description.getOutputDataObjects(activity))
            {
                ((ActivityWithTimeAndData.ActivityWithTimeAndDataBuilder) activityBuilder).outputDataObject(dataObject);
            }

            for (LoggableStringDataObject dataObject : description.getInputDataObjects(activity))
            {
                ((ActivityWithTimeAndData.ActivityWithTimeAndDataBuilder) activityBuilder).inputDataObject(dataObject);
            }

            getNodesToActualMovableBuilders().put(activity.getId(), activityBuilder);
        }
    }
}
