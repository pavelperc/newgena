package org.processmining.models.bpmn_data_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.bpmn_with_time.ExclusiveGatewayWithTime;
import org.processmining.models.bpmn_with_time.SequenceFlowWithTime;
import org.processmining.models.bpmn_with_time.SubProcessWithTime;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.utils.python.PythonRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan on 22.02.2016.
 */
public class ExclusiveGatewayWithTimeAndData extends ExclusiveGatewayWithTime
{
    private String scriptPath;
    private Collection<LoggableStringDataObject> inputDataObjects;
    private Map<String, SequenceFlowWithTime> labelsToOutgoingFlows = new HashMap<>();

    protected ExclusiveGatewayWithTimeAndData(
            Gateway actualGateway, String scriptPath, SubProcessWithTime parentSubProcess,
            List<SequenceFlowWithTime> inputSequenceFlows, List<SequenceFlowWithTime> outputSequenceFlows,
            Collection<LoggableStringDataObject> inputDataObjects)
    {
        super(actualGateway, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
        this.scriptPath = scriptPath;
        this.inputDataObjects = inputDataObjects;

        for (SequenceFlowWithTime outFlow : outputSequenceFlows)
        {
            Flow actualFlow = outFlow.getFlow();
            String label = actualFlow.getTarget().getLabel();
            labelsToOutgoingFlows.put(label, outFlow);
        }
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult movementResult = new MovementResult();

        TokenWithTime consumedToken = consumeToken(movementResult);

        produceToken(movementResult, consumedToken);

        return movementResult;
    }

    protected void produceToken(MovementResult movementResult, TokenWithTime consumedToken)
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

        SequenceFlowWithTime selectedFlow = labelsToOutgoingFlows.get(selectedFlowLabel);

        if (!selectedFlow.hasTokens())
        {
            movementResult.addFilledTokenables(selectedFlow);
        }

        selectedFlow.addToken(new TokenWithTime(consumedToken.getTimestamp()));
    }
}
