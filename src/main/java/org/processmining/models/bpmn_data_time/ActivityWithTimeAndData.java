package org.processmining.models.bpmn_data_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.NodeCallback;
import org.processmining.models.TokenWithTime;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.bpmn_with_time.ActivityWithTime;
import org.processmining.models.bpmn_with_time.MessageFlowWithTime;
import org.processmining.models.bpmn_with_time.SequenceFlowWithTime;
import org.processmining.models.bpmn_with_time.SubProcessWithTime;
import org.processmining.models.descriptions.BPMNWithTimeAndDataDescription;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.time.managers.ExecutionTimeManager;
import org.processmining.utils.LoggingSingletonWithTimeAndData;
import org.processmining.utils.python.PythonRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan on 22.02.2016.
 */
public class ActivityWithTimeAndData extends ActivityWithTime
{
    private String dataScriptPath;
    private List<LoggableStringDataObject> inputDataObjects;
    private Map<String, LoggableStringDataObject> labelsToDataObjects;

    protected ActivityWithTimeAndData(
            Activity actualActivity, String dataScriptPath, SubProcessWithTime parentSubProcess,
            List<SequenceFlowWithTime> inputSequenceFlows, List<SequenceFlowWithTime> outputSequenceFlows,
            List<MessageFlowWithTime> inputMessageFlows, List<MessageFlowWithTime> outputMessageFlows,
            ExecutionTimeManager<Activity> timeManager, boolean separatingStartAndFinish,
            List<LoggableStringDataObject> inputDataObjects,
            List<LoggableStringDataObject> outputDataObjects,
            BPMNWithTimeAndDataDescription description,
            String timeScriptPath)
    {
        super(actualActivity, parentSubProcess,
                inputSequenceFlows, outputSequenceFlows,
                inputMessageFlows, outputMessageFlows,
                timeManager, separatingStartAndFinish,
                description, timeScriptPath);
        this.dataScriptPath = dataScriptPath;
        this.inputDataObjects = inputDataObjects;

        labelsToDataObjects = new HashMap<>();

        for (LoggableStringDataObject outDataObject : outputDataObjects)
        {
            labelsToDataObjects.put(outDataObject.getLabel(), outDataObject);
        }
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult<TokenWithTime> movementResult = new MovementResult<>();

        long startTimestamp = consumeTokens(movementResult);

        if (separatingStartAndFinish)
        {
            LoggingSingletonWithTimeAndData.log((BPMNWithTimeAndDataDescription) getDescription(), trace, getActualActivity().getLabel(), startTimestamp, false, getPoolName(), getLaneName());
        }

        long executionTime = timeManager.getExecutionTime(getActualActivity(), startTimestamp);

        final long completeTimestamp = startTimestamp + executionTime;
        final TokenWithTime intermediateToken = new TokenWithTime(completeTimestamp);

        if (getParentSubProcess() != null)
        {
            ((SubProcessWithTime) getParentSubProcess()).addExtraMovable(intermediateToken);
        }

        final String arguments = compileScriptArguments();

        NodeCallback callback = new NodeCallback()
        {
            @Override
            public MovementResult move(XTrace trace)
            {
                MovementResult<TokenWithTime> movementResult = new MovementResult<>();
                produceTokens(movementResult, intermediateToken.getTimestamp());
                movementResult.addConsumedExtraToken(intermediateToken);

                if (getParentSubProcess() != null)
                {
                    ((SubProcessWithTime) getParentSubProcess()).removeExtraMovable(intermediateToken);
                }

                if (inputDataObjects.isEmpty() && labelsToDataObjects.isEmpty())
                {

                }
                else
                {
                    String response = PythonRunner.run(dataScriptPath, arguments);
                    String[] args = response.split("\"");

                    for (String argument : args)
                    {
                        if (!argument.trim().isEmpty())
                        {
                            String[] keyValue = argument.split(":");
                            String key = keyValue[0];
                            String value = keyValue[1];

                            LoggableStringDataObject dataObject = labelsToDataObjects.get(key);
                            dataObject.write(value);
                        }
                    }
                }

                // BPMNWithTimeAndDataDescription description, XTrace trace, String eventName, long timestamp, boolean isComplete,String group, String resource
                LoggingSingletonWithTimeAndData.log((BPMNWithTimeAndDataDescription) getDescription(), trace, getActualActivity().getLabel(),
                        completeTimestamp, true, getPoolName(), getLaneName());

                return movementResult;
            }
        };

        intermediateToken.setCallback(callback);

        movementResult.addProducedExtraToken(intermediateToken);

        return movementResult;
    }

    private String compileScriptArguments()
    {
        if (inputDataObjects.isEmpty() && labelsToDataObjects.isEmpty())
        {
            return null;
        }

        StringBuilder argumentsBuilder = new StringBuilder();

        argumentsBuilder.append(" activity ");

        for (LoggableStringDataObject dataObject : inputDataObjects)
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(dataObject.getLabel());
            argumentsBuilder.append(':');
            argumentsBuilder.append(dataObject.read());
            argumentsBuilder.append("\" ");
        }

        argumentsBuilder.append("- ");

        for (String dataObjectLabel : labelsToDataObjects.keySet())
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(dataObjectLabel);
            argumentsBuilder.append("\" ");
        }


        return argumentsBuilder.toString();
    }

    public static class ActivityWithTimeAndDataBuilder extends ActivityWithTimeBuilder
    {
        private String dataScriptPath;
        private List<LoggableStringDataObject> inputDataObjects = new ArrayList<>();
        private List<LoggableStringDataObject> outputDataObjects = new ArrayList<>();

        public ActivityWithTimeAndDataBuilder(Activity actualActivity, boolean separatingStartAndFinish,
                                              ExecutionTimeManager<Activity> timeManager, String dataScriptPath,
                                              BPMNWithTimeAndDataDescription description, String timeScriptPath)
        {
            super(actualActivity, separatingStartAndFinish, timeManager, description, timeScriptPath);
            this.dataScriptPath = dataScriptPath;
        }

        public void inputDataObject(LoggableStringDataObject dataObject)
        {
            inputDataObjects.add(dataObject);
        }

        public void outputDataObject(LoggableStringDataObject dataObject)
        {
            outputDataObjects.add(dataObject);
        }

        @Override
        public ActivityWithTime build()
        {
            return new ActivityWithTimeAndData(actualNode, dataScriptPath, (SubProcessWithTime) parentSubProcess,
                    inputSequenceFlows, outputSequenceFlows,
                    inputMessageFlows, outputMessageFlows,
                    timeManager, separatingStartAndFinish,
                    inputDataObjects, outputDataObjects,
                    (BPMNWithTimeAndDataDescription) description, timeScriptPath);
        }
    }
}
