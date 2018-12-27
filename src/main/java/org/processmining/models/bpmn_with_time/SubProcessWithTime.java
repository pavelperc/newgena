package org.processmining.models.bpmn_with_time;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.AbstractSubProcess;
import org.processmining.models.base_bpmn.AbstractSubProcessBuilder;
import org.processmining.models.base_bpmn.SequenceFlow;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;

import java.util.*;

/**
 * Created by Ivan on 16.09.2015.
 */
public class SubProcessWithTime extends AbstractSubProcess<TokenWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime> implements MovableWithTime
{
    private Random random = new Random();
    private Set<TokenWithTime> extraMovables = new HashSet<>();

    protected SubProcessWithTime(
            SubProcess actualSubProcess,
            SubProcessWithTime parentSubProcess, List<? extends SequenceFlowWithTime> inputFlows,
            Collection<? extends SequenceFlowWithTime> outputFlows, List<? extends SequenceFlowWithTime> internalSequenceFlows,
            Collection<? extends SequenceFlowWithTime> dummyFlowsToFrontSubProcessElements,
            Collection<? extends SequenceFlowWithTime> cancelOutputFlows,
            Collection<? extends MessageFlowWithTime> inputMessageFlows,
            Collection<? extends MessageFlowWithTime> outputMessageFlows)
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
    public Long getTimestamp()
    {
        long timestamp = Long.MAX_VALUE;

        for (SequenceFlowWithTime inFlow : getInputFlows())
        {
            TokenWithTime tokenWithTime = inFlow.peekToken();
            long tokenTimestamp = tokenWithTime.getTimestamp();

            if (tokenTimestamp < timestamp)
            {
                timestamp = tokenTimestamp;
            }
        }

        for (MessageFlowWithTime inMessageFlow : getInputMessageFlows())
        {
            TokenWithTime token = inMessageFlow.peekToken();
            long tokenTimestamp = token.getTimestamp();

            if (tokenTimestamp > timestamp)
            {
                timestamp = tokenTimestamp;
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

    protected MovementResult updateState(long timestamp)
    {
        MovementResult movementResult = new MovementResult(false);

        if (!hasInternalTokens() && !nestedSubProcessesAreInProgress())
        {
            produceOutput(movementResult, timestamp);

            if (getParentSubProcess() != null)
            {
                getParentSubProcess().updateState(timestamp);
            }
        }

        return movementResult;
    }

    protected void produceOutput(MovementResult movementResult, long timestamp)
    {
        for (SequenceFlow<TokenWithTime> outFlow : getOutputFlows())
        {
            if (!outFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outFlow);
            }

            outFlow.addToken(createToken(timestamp));
        }

        for (MessageFlowWithTime outputMessageFlow : getOutputMessageFlows())
        {
            if (!outputMessageFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputMessageFlow);
            }

            outputMessageFlow.addToken(createToken(timestamp));
        }

        setInProgress(false);
    }

    @Override
    public void stopSubProcess()
    {
        super.stopSubProcess();
        extraMovables.clear();
    }

    @Override
    protected MovementResult updateState(boolean normalTermination)
    {
        throw new IllegalStateException("You should call the overloaded method");
    }

    @Override
    protected MovementResult cancelSubProcess()
    {
        throw new IllegalStateException("You have to call the overloaded method");
    }

    @Override
    protected MovementResult deleteTokens()
    {
        setInProgress(false);

        MovementResult movementResult = new MovementResult();

        for (SequenceFlow internalSequenceFlow : getInternalSequenceFlows())
        {
            if (internalSequenceFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(internalSequenceFlow);
                internalSequenceFlow.removeAllTokens();
            }
        }

        for (SequenceFlow flow : getDummyFlowsToFrontSubProcessElements())
        {
            if (flow.hasTokens())
            {
                movementResult.addEmptiedTokenable(flow);
                flow.removeAllTokens();
            }
        }

        for (SubProcessWithTime nestedSubProcess : getNestedSubProcesses())
        {
            if (nestedSubProcess.isInProgress())
            {
                MovementResult nestedResult = nestedSubProcess.deleteTokens();
                movementResult.addAllEmptiedTokenables(nestedResult.getEmptiedTokenables());
                movementResult.addAllFilledTokenables(nestedResult.getFilledTokenables());
                movementResult.addConsumedExtraTokens(nestedResult.getConsumedExtraMovables());

            }
        }

        movementResult.addConsumedExtraTokens(extraMovables);

        extraMovables.clear();

        return movementResult;
    }

    protected MovementResult cancelSubProcess(long timestamp)
    {
        MovementResult movementResult = new MovementResult();

        MovementResult nestedResult = deleteTokens();
        movementResult.addAllEmptiedTokenables(nestedResult.getEmptiedTokenables());
        movementResult.addAllFilledTokenables(nestedResult.getFilledTokenables());
        movementResult.addConsumedExtraTokens(nestedResult.getConsumedExtraMovables());
        movementResult.addAllFilledTokenables(nestedResult.getFilledTokenables());

        //TODO is it correct behavior? What do we say in the article?
        for (SequenceFlow<TokenWithTime> cancelOutputFlow : getCancelOutputFlows())
        {
            if (!cancelOutputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(cancelOutputFlow);
            }

            cancelOutputFlow.addToken(createToken(timestamp));
        }

        if (getParentSubProcess() != null)
        {
            getParentSubProcess().updateState(timestamp);
        }

        setInProgress(false);

        return movementResult;
    }

    @Override
    protected TokenWithTime createToken()
    {
        throw new IllegalStateException("You have to call the overloaded method");
    }

    protected TokenWithTime createToken(long timestamp)
    {
        return new TokenWithTime(timestamp);
    }

    public void addExtraMovable(TokenWithTime tokenWithTime)
    {
        extraMovables.add(tokenWithTime);
    }

    public void removeExtraMovable(TokenWithTime tokenWithTime)
    {
        extraMovables.remove(tokenWithTime);
    }


    @Override
    public MovementResult move(XTrace trace) //TODO ��������, ����� ������� ���� ��������� ����������, � �� ����������� ���
    {           //TODO � ����� �� ��������� ��� �����������?
        if (isInProgress())
        {
            throw new IllegalStateException("Illegal state of a sub process");
        }

        setInProgress(true);

        MovementResult movementResult = new MovementResult();

        List<SequenceFlowWithTime> earliestInSequenceFlows = findInFlowsWithEarliestTokens();
        SequenceFlowWithTime inFlow = selectFlowToMoveFrom(earliestInSequenceFlows);

        TokenWithTime consumedSequenceFlowToken = inFlow.consumeToken();

        long startTimestamp = consumedSequenceFlowToken.getTimestamp();

        if (!inFlow.hasTokens())
        {
            movementResult.addEmptiedTokenable(inFlow);
        }

        for (MessageFlowWithTime inMessageFlow : getInputMessageFlows())
        {
            TokenWithTime token = inMessageFlow.consumeToken();
            long tokenTimestamp = token.getTimestamp();

            if (tokenTimestamp > startTimestamp)
            {
                startTimestamp = tokenTimestamp;
            }

            if (!inMessageFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(inMessageFlow);
            }
        }

        for (SequenceFlowWithTime dummyFlow : getDummyFlowsToFrontSubProcessElements())
        {
            if (!dummyFlow.hasTokens())
            {
                movementResult.addFilledTokenables(dummyFlow);
            }
            dummyFlow.addToken(new TokenWithTime(startTimestamp));
        }

        return movementResult;
    }


    private List<SequenceFlowWithTime> findInFlowsWithEarliestTokens()
    {
        List<SequenceFlowWithTime> inFlowsWithEarliestTokens = new ArrayList<SequenceFlowWithTime>();
        long time = 0;

        for (SequenceFlowWithTime inFlow : getInputFlows())
        {
            if (inFlow.hasTokens())
            {
                long tokenTime = inFlow.peekToken().getTimestamp();

                if (inFlowsWithEarliestTokens.isEmpty())
                {
                    inFlowsWithEarliestTokens.add(inFlow);
                    time = tokenTime;
                }
                else
                {
                    if (time == tokenTime)
                    {
                        inFlowsWithEarliestTokens.add(inFlow);
                    }
                    else
                    {
                        if (time > tokenTime)
                        {
                            inFlowsWithEarliestTokens.clear();
                            inFlowsWithEarliestTokens.add(inFlow);
                            time = tokenTime;
                        }
                    }
                }
            }
        }

        return inFlowsWithEarliestTokens;
    }

    protected <T extends SequenceFlow> T selectFlowToMoveFrom(List<T> inputFlowsWithTokens)
    {
        int index = random.nextInt(inputFlowsWithTokens.size());
        return inputFlowsWithTokens.get(index);
    }

    public static class SubProcessWithTimeBuilder extends AbstractSubProcessBuilder<TokenWithTime, SequenceFlowWithTime, MessageFlowWithTime, SubProcessWithTime>
    {
        public SubProcessWithTimeBuilder(SubProcess actualSubProcess)
        {
            super(actualSubProcess);
        }

        @Override
        public SubProcessWithTime build()
        {
            return new SubProcessWithTime(
                    actualNode, (SubProcessWithTime) parentSubProcess,
                    inputSequenceFlows,
                    outputSequenceFlows, getInternalFlows(),
                    getDummyFlowsToFrontSubProcessElements(),
                    getCancelOutputFlows(), inputMessageFlows,
                    outputMessageFlows);
        }
    }
}
