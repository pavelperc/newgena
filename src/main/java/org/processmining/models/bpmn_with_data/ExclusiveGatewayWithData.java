package org.processmining.models.bpmn_with_data;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.utils.python.PythonRunner;

import java.util.*;

/**
 * Created by Ivan on 29.04.2015.
 */
public class ExclusiveGatewayWithData extends ExclusiveChoiceGateway
{
    private String scriptPath;
    private Collection<LoggableStringDataObject> inputDataObjects;
    private Map<String, SimpleSequenceFlow> labelsToOutgoingFlows = new HashMap<>();

    protected ExclusiveGatewayWithData(
            org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway,
            SubProcess parentSubProcess,
            List<SimpleSequenceFlow> inputSequenceFlows,
            Collection<LoggableStringDataObject> inputDataObjects, String scriptPath,
            List<SimpleSequenceFlow> outputSequenceFlows)
    {
        super(actualGateway, parentSubProcess, inputSequenceFlows, Collections.EMPTY_LIST);

        this.inputDataObjects = inputDataObjects;
        this.scriptPath = scriptPath;

        for (SimpleSequenceFlow outFlow : outputSequenceFlows)
        {
            Flow actualFlow = outFlow.getFlow();
            String label = actualFlow.getTarget().getLabel();
            labelsToOutgoingFlows.put(label, outFlow);
        }
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        StringBuilder argumentsBuilder = new StringBuilder();

        argumentsBuilder.append(" gateway ");

        for (LoggableStringDataObject dataObject : inputDataObjects)
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(dataObject.getLabel());
            argumentsBuilder.append(':');
            argumentsBuilder.append(dataObject.read());
            argumentsBuilder.append("\" ");
        }

        argumentsBuilder.append("- ");

        for (String label : labelsToOutgoingFlows.keySet())
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(label);
            argumentsBuilder.append("\" ");
        }

        String arguments = argumentsBuilder.toString();
        String selectedFlowLabel = PythonRunner.run(scriptPath, arguments);

        SimpleSequenceFlow selectedFlow = labelsToOutgoingFlows.get(selectedFlowLabel);

        MovementResult movementResult = new MovementResult();
        consumeToken(movementResult);
        produceToken(movementResult, selectedFlow);

        return movementResult;
    }

    @Override
    public boolean checkAvailability()
    {
        return super.checkAvailability();
    }

    public static class ExclusiveGatewayWithDataBuilder extends
            AbstractNodeBuilder<Gateway, org.processmining.models.graphbased.directed.bpmn.elements.Gateway, SimpleSequenceFlow, SimpleMessageFlow>
    {
        private String scriptPath;
        private List<LoggableStringDataObject> inputDataObjects = new ArrayList<>();

        public ExclusiveGatewayWithDataBuilder(
                org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway,
                String scriptPath)
        {
            super(actualGateway);
            this.scriptPath = scriptPath;
        }

        @Override
        public Gateway build()
        {
            return new ExclusiveGatewayWithData(
                    actualNode, (SubProcess) parentSubProcess,
                    inputSequenceFlows,
                    inputDataObjects, scriptPath,
                    outputSequenceFlows);
        }

        public void inputDataObject(LoggableStringDataObject dataObject)
        {
            inputDataObjects.add(dataObject);
        }

        @Override
        public void outgoingMessageFlow(SimpleMessageFlow outputFlow)
        {
            //just ignore
        }
    }
}
