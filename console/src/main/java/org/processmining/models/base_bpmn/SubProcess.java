package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;

import java.util.Collection;
import java.util.List;

/**
 * Created by Ivan Shugurov on 09.02.2015.
 */
public class SubProcess extends AbstractSubProcess<Token, SimpleSequenceFlow, SimpleMessageFlow, SubProcess>
{
    protected SubProcess(
            org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess,
            SubProcess parentSubProcess,
            Collection<? extends SimpleSequenceFlow> inputFlows,
            Collection<? extends SimpleSequenceFlow> outputFlows,
            Collection<? extends SimpleSequenceFlow> internalSequenceFlows,
            Collection<? extends SimpleSequenceFlow> dummyFlowsToFrontSubProcessElements,
            Collection<? extends SimpleSequenceFlow> cancelOutputFlows,
            Collection<? extends SimpleMessageFlow> inputMessageFlows,
            Collection<? extends SimpleMessageFlow> outputMessageFlows)
    {
        super(
                actualSubProcess, parentSubProcess,
                inputFlows, outputFlows,
                internalSequenceFlows,
                dummyFlowsToFrontSubProcessElements,
                cancelOutputFlows, inputMessageFlows,
                outputMessageFlows);
    }

    @Override
    protected Token createToken()
    {
        return new Token();
    }

    @Override
    public MovementResult move(XTrace trace)  //TODO � ����� �� ��������� ��� ���������� ����������?
    {
        MovementResult movementResult = new MovementResult();

        if (isInProgress())
        {
            throw new IllegalStateException("Illegal state of a sub process");
        }
        setInProgress(true);

        List<? extends SequenceFlow> inputFlowsWithTokens = getSequenceFlowsWithTokens();

        SequenceFlow inputSequenceFlowWithToken = pickRandomElement(inputFlowsWithTokens);
        inputSequenceFlowWithToken.consumeToken();

        for (MessageFlow inputMessageFlow : getInputMessageFlows())
        {
            inputMessageFlow.consumeToken();

            if (!inputMessageFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(inputMessageFlow);
            }
        }

        if (!inputSequenceFlowWithToken.hasTokens())
        {
            movementResult.addEmptiedTokenable(inputSequenceFlowWithToken);
        }

        for (SimpleSequenceFlow dummyFlow : getDummyFlowsToFrontSubProcessElements())
        {
            dummyFlow.addToken(new Token());
            movementResult.addFilledTokenables(dummyFlow);
        }

        return movementResult;
    }


    public static class SubProcessBuilder extends AbstractSubProcessBuilder<Token, SimpleSequenceFlow, SimpleMessageFlow, SubProcess>
    {
        public SubProcessBuilder(org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess)
        {
            super(actualSubProcess);
        }

        @Override
        public SubProcess build()
        {
            return new SubProcess(
                    actualNode, (SubProcess) parentSubProcess,
                    inputSequenceFlows,
                    outputSequenceFlows, getInternalFlows(),
                    getDummyFlowsToFrontSubProcessElements(),
                    getCancelOutputFlows(), inputMessageFlows,
                    outputMessageFlows);
        }
    }
}
