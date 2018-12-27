package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;

import java.util.List;

/**
 * Created by Ivan Shugurov on 21.12.2014.
 */
public class ParallelGateway extends Gateway
{
    protected ParallelGateway(
            org.processmining.models.graphbased.directed.bpmn.elements.Gateway actualGateway,
            AbstractSubProcess parentSubProcess,
            List<? extends SequenceFlow> inputSequenceFlows, List<? extends SequenceFlow> outputSequenceFlows)
    {
        super(actualGateway, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult movementResult = new MovementResult();

        for (SequenceFlow inputFlow : getInputSequenceFlows())
        {
            inputFlow.consumeToken();

            if (!inputFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(inputFlow);
            }
        }

        for (SequenceFlow outputFlow : getOutputSequenceFlows())
        {
            if (!outputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputFlow);
            }

            outputFlow.addToken(new Token());
        }

        return movementResult;
    }

    @Override
    public boolean checkAvailability()
    {
        if (getInputSequenceFlows().isEmpty())
        {
            return false;
        }

        for (SequenceFlow inputFlow : getInputSequenceFlows())
        {
            if (!inputFlow.hasTokens())
            {
                return false;
            }
        }

        return true;
    }
}
