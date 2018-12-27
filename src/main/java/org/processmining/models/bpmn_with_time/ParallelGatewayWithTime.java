package org.processmining.models.bpmn_with_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.ParallelGateway;
import org.processmining.models.base_bpmn.SequenceFlow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.List;

/**
 * Created by Ivan on 31.08.2015.
 */
public class ParallelGatewayWithTime extends ParallelGateway implements MovableWithTime
{

    public ParallelGatewayWithTime(
            Gateway actualGateway, SubProcessWithTime parentSubProcess,
            List<SequenceFlowWithTime> inputSequenceFlows,
            List<SequenceFlowWithTime> outputSequenceFlows)
    {
        super(actualGateway, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MovementResult move(XTrace trace)
    {
        //TODO ����������� ��� "���������" ��� ��� ���
        MovementResult<TokenWithTime> movementResult = new MovementResult<TokenWithTime>();
        TokenWithTime latestToken = null;

        for (SequenceFlowWithTime inFlow : getInputSequenceFlows())
        {
            TokenWithTime token = inFlow.consumeToken();

            if (!inFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(inFlow);
            }

            if (latestToken == null)
            {
                latestToken = token;
            }
            else
            {
                int comparisonWithEarliestToken = latestToken.compareTo(token);

                if (comparisonWithEarliestToken < 0)
                {
                    latestToken = token;
                }
            }
        }


        long time = latestToken.getTimestamp();

        for (SequenceFlow outputFlow : getOutputSequenceFlows())
        {
            if (!outputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputFlow);
            }

            outputFlow.addToken(new TokenWithTime(time));
        }

        return movementResult;
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
        long earliestPossibleFiring = Long.MIN_VALUE;

        for (SequenceFlowWithTime inFlow : getInputSequenceFlows())
        {
            TokenWithTime tokenWithTime = inFlow.peekToken();
            long tokenTimestamp = tokenWithTime.getTimestamp();

            if (earliestPossibleFiring < tokenTimestamp)
            {
                earliestPossibleFiring = tokenTimestamp;
            }
        }

        if (earliestPossibleFiring == Long.MIN_VALUE)
        {
            return null;
        }
        else
        {
            return earliestPossibleFiring;
        }
    }
}
