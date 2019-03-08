package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Shugurov on 21.12.2014.
 */
public class ExclusiveChoiceGateway extends Gateway
{
    protected ExclusiveChoiceGateway(
            org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway,
            AbstractSubProcess parentSubProcess,
            List<? extends SequenceFlow> inputSequenceFlows,
            List<? extends SequenceFlow> outputSequenceFlows)
    {
        super(actualGateway, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult movementResult = new MovementResult();

        Token consumedToken = consumeToken(movementResult);

        produceToken(movementResult, consumedToken);

        return movementResult;
    }

    protected void produceToken(MovementResult movementResult, Token consumedToken)//TODO неудачный каст...
    {
        SequenceFlow flowToAddTokenTo = selectRandomFlow(getOutputSequenceFlows());

        produceToken(movementResult, flowToAddTokenTo);
    }

    protected void produceToken(MovementResult movementResult, SequenceFlow flowToAddTokenTo)
    {
        if (!flowToAddTokenTo.hasTokens())
        {
            movementResult.addFilledTokenables(flowToAddTokenTo);
        }

        flowToAddTokenTo.addToken(new Token());
    }

    protected Token consumeToken(MovementResult movementResult)
    {
        List<? extends SequenceFlow> inputFlows = getInputSequenceFlows();
        List<SequenceFlow> inputFlowsWithTokens = new ArrayList<SequenceFlow>();

        for (SequenceFlow inputFlow : inputFlows)
        {
            if (inputFlow.hasTokens())
            {
                inputFlowsWithTokens.add(inputFlow);
            }
        }

        SequenceFlow flowToMoveFrom = selectRandomFlow(inputFlowsWithTokens);
        Token consumedToken = flowToMoveFrom.consumeToken();

        if (!flowToMoveFrom.hasTokens())
        {
            movementResult.addEmptiedTokenable(flowToMoveFrom);
        }

        return consumedToken;
    }

    protected <T extends SequenceFlow> T selectRandomFlow(List<T> flows)
    {
        if (flows.size() == 1)
        {
            return flows.get(0);
        }
        else
        {
            int index = random.nextInt(flows.size());
            return flows.get(index);
        }
    }

    @Override
    public boolean checkAvailability()
    {
        List<? extends SequenceFlow> inputFlows = getInputSequenceFlows();

        for (SequenceFlow inputFlow : inputFlows)
        {
            if (inputFlow.hasTokens())
            {
                return true;
            }
        }

        return false;
    }
}
