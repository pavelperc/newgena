package org.processmining.models.bpmn_with_data;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.AbstractSubProcessBuilder;
import org.processmining.models.base_bpmn.SimpleMessageFlow;
import org.processmining.models.base_bpmn.SimpleSequenceFlow;
import org.processmining.models.base_bpmn.SubProcess;
import org.processmining.utils.python.PythonRunner;

import java.util.*;

/**
 * Created by Ivan on 22.04.2015.
 */
public class SubProcessWithDataObjects extends SubProcess
{
    //TODO implement case when sub process does npt have incoming/outgoing data associations, but has
    private List<LoggableStringDataObject> inputDataObjects;
    private String[] readData;
    private Map<String, LoggableStringDataObject> labelsToOutputDataObjects;
    private String scriptPath;

    protected SubProcessWithDataObjects(
            org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess,
            Collection<SimpleSequenceFlow> inputFlows, Collection<SimpleSequenceFlow> outputFlows,
            Collection<SimpleSequenceFlow> internalSequenceFlows, Collection<SimpleSequenceFlow> dummyFlowsToFrontSubProcessElements,
            Collection<SimpleSequenceFlow> cancelOutputFlows, Collection<SimpleMessageFlow> inputMessageFlows,
            Collection<SimpleMessageFlow> outputMessageFlows,
            SubProcess parentSubProcess, List<LoggableStringDataObject> inputDataObjects,
            Map<String, LoggableStringDataObject> labelsToOutputDataObjects, String scriptPath)
    {
        super(
                actualSubProcess, parentSubProcess,
                inputFlows, outputFlows,
                internalSequenceFlows, dummyFlowsToFrontSubProcessElements,
                cancelOutputFlows,
                inputMessageFlows, outputMessageFlows);

        this.inputDataObjects = inputDataObjects;
        this.labelsToOutputDataObjects = labelsToOutputDataObjects;
        this.scriptPath = scriptPath;
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

    @Override
    protected void produceOutput(MovementResult movementResult)
    {
        super.produceOutput(movementResult);

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

    public static class SubProcessWithDataObjectsBuilder extends
            AbstractSubProcessBuilder<Token, SimpleSequenceFlow, SimpleMessageFlow, SubProcessWithDataObjects>
            implements BuilderWithData
    {
        private List<LoggableStringDataObject> inputDataObjects = new ArrayList<>();
        private Map<String, LoggableStringDataObject> labelsToOutputDataObjects = new HashMap<>();
        private String scriptPath;

        public SubProcessWithDataObjectsBuilder(
                org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess,
                String scriptPath)
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
        public SubProcessWithDataObjects build()
        {
            return new SubProcessWithDataObjects(
                    actualNode, inputSequenceFlows,
                    outputSequenceFlows, getInternalFlows(),
                    getDummyFlowsToFrontSubProcessElements(),
                    getCancelOutputFlows(), inputMessageFlows,
                    outputMessageFlows,
                    (SubProcess) parentSubProcess,
                    inputDataObjects,
                    labelsToOutputDataObjects,
                    scriptPath);
        }
    }

}
