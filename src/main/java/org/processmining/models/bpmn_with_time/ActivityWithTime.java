package org.processmining.models.bpmn_with_time;


import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.NodeCallback;
import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.AbstractNodeBuilder;
import org.processmining.models.base_bpmn.Activity;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.time.managers.ExecutionTimeManager;
import org.processmining.utils.LoggingSingletonWithTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 12.08.2015.
 */
public class ActivityWithTime extends Activity implements MovableWithTime
{
    protected ExecutionTimeManager<org.processmining.models.graphbased.directed.bpmn.elements.Activity> timeManager;
    protected boolean separatingStartAndFinish;
    private BPMNWithTimeGenerationDescription description;
    private String timeScriptPath;

    protected ActivityWithTime(
            org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity,
            SubProcessWithTime parentSubProcess,
            List<SequenceFlowWithTime> inputSequenceFlows, List<SequenceFlowWithTime> outputSequenceFlows,
            List<MessageFlowWithTime> inputMessageFlows, List<MessageFlowWithTime> outputMessageFlows,
            ExecutionTimeManager<org.processmining.models.graphbased.directed.bpmn.elements.Activity> timeManager,
            boolean separatingStartAndFinish, BPMNWithTimeGenerationDescription description,
            String timeScriptPath)
    {
        super(
                actualActivity, parentSubProcess,
                inputSequenceFlows, outputSequenceFlows,
                inputMessageFlows, outputMessageFlows);
        this.timeManager = timeManager;
        this.separatingStartAndFinish = separatingStartAndFinish;
        this.description = description;
        this.timeScriptPath = timeScriptPath;
    }

    protected String getTimeScriptPath()
    {
        return timeScriptPath;
    }

    public BPMNWithTimeGenerationDescription getDescription()
    {
        return description;
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult<TokenWithTime> movementResult = new MovementResult<>();

        long startTimestamp = consumeTokens(movementResult);

        if (separatingStartAndFinish)
        {
            LoggingSingletonWithTime.log(description, trace, getActualActivity().getLabel(), startTimestamp, false, getPoolName(), getLaneName());
        }

        long executionTime = timeManager.getExecutionTime(getActualActivity(), startTimestamp);

        final long completeTimestamp = startTimestamp + executionTime;
        final TokenWithTime intermediateToken = new TokenWithTime(completeTimestamp);

        if (getParentSubProcess() != null)
        {
            ((SubProcessWithTime) getParentSubProcess()).addExtraMovable(intermediateToken);
        }

        NodeCallback callback = new NodeCallback()
        {
            @Override
            public MovementResult move(XTrace trace)
            {
                MovementResult<TokenWithTime> movementResult = new MovementResult<>();
                produceTokens(movementResult, intermediateToken.getTimestamp());
                movementResult.addConsumedExtraToken(intermediateToken);

                LoggingSingletonWithTime.log(description, trace, getActualActivity().getLabel(), completeTimestamp, true, getPoolName(), getLaneName());

                if (getParentSubProcess() != null)
                {
                    ((SubProcessWithTime) getParentSubProcess()).removeExtraMovable(intermediateToken);
                }

                return movementResult;
            }
        };

        intermediateToken.setCallback(callback);

        movementResult.addProducedExtraToken(intermediateToken);

        return movementResult;
    }

    protected long consumeTokens(MovementResult<TokenWithTime> movementResult)
    {
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

        return startTimestamp;
    }

    protected List<SequenceFlowWithTime> findInFlowsWithEarliestTokens()
    {
        List<SequenceFlowWithTime> inFlowsWithEarliestTokens = new ArrayList<SequenceFlowWithTime>();
        long time = 0;

        for (SequenceFlowWithTime inFlow : getInputSequenceFlows())
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

    @Override
    protected void produceTokens(MovementResult movementResult)
    {
        //that's ok
    }

    protected void produceTokens(MovementResult movementResult, long timestamp)
    {
        for (SequenceFlowWithTime outputFlow : getOutputSequenceFlows())
        {
            if (!outputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputFlow);
            }

            outputFlow.addToken(new TokenWithTime(timestamp));
        }

        for (MessageFlowWithTime outputMessageFlow : getOutputMessageFlows())
        {
            if (!outputMessageFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputMessageFlow);
            }

            outputMessageFlow.addToken(new TokenWithTime(timestamp));
        }
    }

    @Override
    protected TokenWithTime consumeToken(MovementResult movementResult)
    {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<SequenceFlowWithTime> getInputSequenceFlows()
    {
        return (List<SequenceFlowWithTime>) super.getInputSequenceFlows();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<SequenceFlowWithTime> getOutputSequenceFlows()
    {
        return (List<SequenceFlowWithTime>) super.getOutputSequenceFlows();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<MessageFlowWithTime> getInputMessageFlows()
    {
        return (List<MessageFlowWithTime>) super.getInputMessageFlows();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<MessageFlowWithTime> getOutputMessageFlows()
    {
        return (List<MessageFlowWithTime>) super.getOutputMessageFlows();
    }

    @Override
    public Long getTimestamp()
    {
        long timestamp = Long.MAX_VALUE;

        for (SequenceFlowWithTime inFlow : getInputSequenceFlows())
        {
            if (inFlow.hasTokens())
            {
                TokenWithTime tokenWithTime = inFlow.peekToken();
                long tokenTimestamp = tokenWithTime.getTimestamp();

                if (tokenTimestamp < timestamp)
                {
                    timestamp = tokenTimestamp;
                }
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

    public static class ActivityWithTimeBuilder extends AbstractNodeBuilder<
            ActivityWithTime, org.processmining.models.graphbased.directed.bpmn.elements.Activity,
            SequenceFlowWithTime, MessageFlowWithTime>
    {
        protected boolean separatingStartAndFinish;
        protected ExecutionTimeManager<org.processmining.models.graphbased.directed.bpmn.elements.Activity> timeManager;
        protected BPMNWithTimeGenerationDescription description;
        protected String timeScriptPath;

        public ActivityWithTimeBuilder(
                org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity,
                boolean separatingStartAndFinish,
                ExecutionTimeManager<org.processmining.models.graphbased.directed.bpmn.elements.Activity> timeManager,
                BPMNWithTimeGenerationDescription description,
                String timeScriptPath)
        {
            super(actualActivity);
            this.timeManager = timeManager;
            this.description = description;
            this.timeScriptPath = timeScriptPath;

            if (actualActivity == null)
            {
                throw new NullPointerException("Activity cannot be null");
            }

            this.separatingStartAndFinish = separatingStartAndFinish;
        }

        @Override
        public ActivityWithTime build()
        {
            return new ActivityWithTime(
                    actualNode, (SubProcessWithTime) parentSubProcess,
                    inputSequenceFlows, outputSequenceFlows,
                    inputMessageFlows, outputMessageFlows,
                    timeManager, separatingStartAndFinish,
                    description, timeScriptPath);
        }

    }
}
