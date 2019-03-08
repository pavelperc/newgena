package org.processmining.models.bpmn_data_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.AbstractSubProcessBuilder;
import org.processmining.models.bpmn_with_data.BuilderWithData;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.bpmn_with_time.MessageFlowWithTime;
import org.processmining.models.bpmn_with_time.SequenceFlowWithTime;
import org.processmining.models.bpmn_with_time.SubProcessWithTime;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.utils.python.PythonRunner;

import java.util.*;

/**
 * Created by Ivan on 04.03.2016.
 */
public class SubProcessWithTimeAndData extends SubProcessWithTime
{
    private List<LoggableStringDataObject> inputDataObjects;
    private String[] readData;
    private Map<String, LoggableStringDataObject> labelsToOutputDataObjects;
    private String scriptPath;

    protected SubProcessWithTimeAndData(SubProcess actualSubProcess, SubProcessWithTime parentSubProcess,
                                        List<? extends SequenceFlowWithTime> inputFlows,
                                        Collection<? extends SequenceFlowWithTime> outputFlows,
                                        List<? extends SequenceFlowWithTime> internalSequenceFlows,
                                        Collection<? extends SequenceFlowWithTime> dummyFlowsToFrontSubProcessElements,
                                        Collection<? extends SequenceFlowWithTime> cancelOutputFlows,
                                        Collection<? extends MessageFlowWithTime> inputMessageFlows,
                                        Collection<? extends MessageFlowWithTime> outputMessageFlows,
                                        List<LoggableStringDataObject> inputDataObjects,
                                        String scriptPath, Map<String, LoggableStringDataObject> labelsToOutputDataObjects)
    {
        super(actualSubProcess, parentSubProcess, inputFlows, outputFlows, internalSequenceFlows,
                dummyFlowsToFrontSubProcessElements, cancelOutputFlows, inputMessageFlows, outputMessageFlows);

        this.inputDataObjects = inputDataObjects;
        this.labelsToOutputDataObjects = labelsToOutputDataObjects;
        this.scriptPath = scriptPath;
    }

    @Override
    protected void produceOutput(MovementResult movementResult, long timestamp)
    {
        super.produceOutput(movementResult, timestamp);

        StringBuilder argumentsBuilder = new StringBuilder();

        argumentsBuilder.append(" subprocess ");

        for (int i = 0; i < readData.length; i++)
        {
            String data = readData[i];
            String dataLabel = inputDataObjects.get(i).getLabel();

            argumentsBuilder.append('"');
            argumentsBuilder.append(dataLabel);
            argumentsBuilder.append(':');
            argumentsBuilder.append(data);
            argumentsBuilder.append("\" ");
        }

        argumentsBuilder.append("- ");

        for (String dataObjectLabel : labelsToOutputDataObjects.keySet())
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(dataObjectLabel);
            argumentsBuilder.append("\" ");
        }

        String arguments = argumentsBuilder.toString();

        String response = PythonRunner.run(scriptPath, arguments);
        String[] args = response.split("\"");

        for (String argument : args)
        {
            if (!argument.trim().isEmpty())
            {
                String[] keyValue = argument.split(":");
                String key = keyValue[0];
                String value = keyValue[1];

                LoggableStringDataObject dataObject = labelsToOutputDataObjects.get(key);
                dataObject.write(value);
            }
        }

        readData = null;
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        readData = new String[inputDataObjects.size()];

        for (int i = 0; i < readData.length; i++)
        {
            readData[i] = inputDataObjects.get(i).read();
        }

        return super.move(trace);
    }

    public static class SubProcessWithTimeAndDataBuilder extends
            AbstractSubProcessBuilder<TokenWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime> implements BuilderWithData
    {
        private List<LoggableStringDataObject> inputDataObjects = new ArrayList<>();
        private Map<String, LoggableStringDataObject> labelsToOutputDataObjects = new HashMap<>();
        private String scriptPath;

        public SubProcessWithTimeAndDataBuilder(SubProcess actualSubProcess, String scriptPath)
        {
            super(actualSubProcess);
            this.scriptPath = scriptPath;
        }

        @Override
        public void inputDataObject(LoggableStringDataObject inputDataObject)
        {
            inputDataObjects.add(inputDataObject);
        }

        @Override
        public void outputDataObject(LoggableStringDataObject outputDataObject)
        {
            labelsToOutputDataObjects.put(outputDataObject.getLabel(), outputDataObject);
        }

        @Override
        public SubProcessWithTime build()
        {
            return new SubProcessWithTimeAndData(
                    actualNode, (SubProcessWithTime) parentSubProcess,
                    inputSequenceFlows,
                    outputSequenceFlows, getInternalFlows(),
                    getDummyFlowsToFrontSubProcessElements(),
                    getCancelOutputFlows(), inputMessageFlows,
                    outputMessageFlows,
                    inputDataObjects, scriptPath, labelsToOutputDataObjects);
        }
    }
}
