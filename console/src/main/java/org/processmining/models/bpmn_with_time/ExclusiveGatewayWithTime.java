package org.processmining.models.bpmn_with_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.ExclusiveChoiceGateway;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 25.08.2015.
 */
public class ExclusiveGatewayWithTime extends ExclusiveChoiceGateway implements MovableWithTime
{
    protected ExclusiveGatewayWithTime(
            Gateway actualGateway,
            SubProcessWithTime parentSubProcess,
            List<SequenceFlowWithTime> inputSequenceFlows,
            List<SequenceFlowWithTime> outputSequenceFlows)
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

    @Override
    protected void produceToken(MovementResult movementResult, Token consumedToken)
    {
        TokenWithTime consumedTokenWithTime = (TokenWithTime) consumedToken;

        List<SequenceFlowWithTime> outFlows = getOutputSequenceFlows();

        SequenceFlowWithTime flow = selectRandomFlow(outFlows);

        if (!flow.hasTokens())
        {
            movementResult.addFilledTokenables(flow);
        }

        flow.addToken(new TokenWithTime(consumedTokenWithTime.getTimestamp()));
    }

    @Override
    protected TokenWithTime consumeToken(MovementResult movementResult)
    {
        List<SequenceFlowWithTime> inputFlows = getInputSequenceFlows();

        List<SequenceFlowWithTime> flowsWithEarliestTokens = new ArrayList<SequenceFlowWithTime>();
        long earliestTimestamp = Long.MAX_VALUE;


        for (SequenceFlowWithTime flow : inputFlows)
        {
            if (flow.hasTokens())
            {
                TokenWithTime token = flow.peekToken();

                if (token.getTimestamp() < earliestTimestamp)
                {
                    flowsWithEarliestTokens.clear();
                    earliestTimestamp = token.getTimestamp();
                    flowsWithEarliestTokens.add(flow);
                }
                else
                {
                    if (token.getTimestamp() == earliestTimestamp)
                    {
                        flowsWithEarliestTokens.add(flow);
                    }
                }
            }
        }


        SequenceFlowWithTime flowToMoveFrom = selectRandomFlow(flowsWithEarliestTokens);
        TokenWithTime consumedToken = flowToMoveFrom.consumeToken();

        if (!flowToMoveFrom.hasTokens())
        {
            movementResult.addEmptiedTokenable(flowToMoveFrom);
        }

        return consumedToken;

    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SequenceFlowWithTime> getOutputSequenceFlows()
    {
        return (List<SequenceFlowWithTime>) super.getOutputSequenceFlows();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SequenceFlowWithTime> getInputSequenceFlows()
    {
        return (List<SequenceFlowWithTime>) super.getInputSequenceFlows();
    }

    @Override
    public Long getTimestamp()
    {
        long timestamp = Long.MAX_VALUE;

        for (SequenceFlowWithTime inFlow : getInputSequenceFlows())
        {
            if (inFlow.hasTokens())
            {
                TokenWithTime token = inFlow.peekToken();
                long tokenTimestamp = token.getTimestamp();

                if (tokenTimestamp < timestamp)
                {
                    timestamp = tokenTimestamp;
                }
            }
        }

        if (timestamp == Long.MAX_VALUE)
        {
            return null;
        }
        else
        {
            return timestamp;
        }
    }
}
