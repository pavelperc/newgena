package org.processmining.models.base_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.Movable;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.utils.BPMNLoggingSingleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ivan Shugurov on 21.12.2014.
 */
public class Activity implements Movable
{
    private static final Random random = new Random();
    private final org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity;
    private final AbstractSubProcess parentSubProcess;
    private final List<? extends MessageFlow> inputMessageFlows;
    private final List<? extends MessageFlow> outputMessageFlows;
    private List<? extends SequenceFlow> inputSequenceFlows;
    private List<? extends SequenceFlow> outputSequenceFlows;
    private String poolName;
    private String laneName;

    protected Activity(
            org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity,
            AbstractSubProcess parentSubProcess,
            List<? extends SequenceFlow> inputSequenceFlows, List<? extends SequenceFlow> outputSequenceFlows,
            List<? extends MessageFlow> inputMessageFlows, List<? extends MessageFlow> outputMessageFlows)
    {
        this.actualActivity = actualActivity;
        this.parentSubProcess = parentSubProcess;
        this.inputSequenceFlows = new ArrayList<>(inputSequenceFlows);
        this.outputSequenceFlows = new ArrayList<>(outputSequenceFlows);
        this.inputMessageFlows = new ArrayList<>(inputMessageFlows);
        this.outputMessageFlows = new ArrayList<>(outputMessageFlows);

        Swimlane pool = actualActivity.getParentPool();

        if (pool == null)
        {
            poolName = "";
        }
        else
        {
            poolName = pool.getLabel();
        }

        org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess = actualActivity.getParentSubProcess();

        if (subProcess == null)
        {
            Swimlane lane = actualActivity.getParentLane();

            if (lane == null)
            {
                laneName = "";
            }
            else
            {
                laneName = lane.getLabel();
            }
        }
        else
        {
            Swimlane parentLane = subProcess.getParentLane();

            if (parentLane == null)
            {
                this.laneName = "";
            }
            else
            {
                this.laneName = parentLane.getLabel();
            }
        }
    }

    public String getPoolName()
    {
        return poolName;
    }

    public String getLaneName()
    {
        return laneName;
    }

    protected AbstractSubProcess getParentSubProcess()
    {
        return parentSubProcess;
    }

    @Override
    @SuppressWarnings("unchecked")

    public MovementResult move(XTrace trace)
    {
        MovementResult movementResult = new MovementResult();

        consumeToken(movementResult);

        log(trace);

        if (parentSubProcess != null && outputSequenceFlows.isEmpty())
        {
            MovementResult subProcessMovementResult = parentSubProcess.updateState(true);
            movementResult.addAllEmptiedTokenables(subProcessMovementResult.getEmptiedTokenables());
            movementResult.addAllFilledTokenables(subProcessMovementResult.getFilledTokenables());
        }

        produceTokens(movementResult);

        return movementResult;
    }

    protected void log(XTrace trace)
    {
        BPMNLoggingSingleton loggingSingleton = getLoggingSingleton();

        String parent;

        if (getParentSubProcess() == null)
        {
            parent = null;
        }
        else
        {
            parent = getParentSubProcess().getLabel();
        }

        loggingSingleton.log(trace, actualActivity, poolName, laneName, parent);
    }

    protected BPMNLoggingSingleton getLoggingSingleton()
    {
        return BPMNLoggingSingleton.getBPMNSingleton();
    }

    @SuppressWarnings("unchecked")
    protected void produceTokens(MovementResult movementResult)
    {
        for (SequenceFlow outputFlow : outputSequenceFlows)
        {
            if (!outputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputFlow);
            }

            outputFlow.addToken(new Token());
        }

        for (MessageFlow outputMessageFlow : outputMessageFlows)
        {
            if (!outputMessageFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputMessageFlow);
            }

            outputMessageFlow.addToken(new Token());
        }
    }

    //TODO ����� �� ������������ ��������?
    protected Token consumeToken(MovementResult movementResult)
    {
        for (MessageFlow inputMessageFlow : inputMessageFlows)
        {
            inputMessageFlow.consumeToken();

            if (!inputMessageFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(inputMessageFlow);
            }
        }

        if (inputSequenceFlows.isEmpty())
        {
            return null;
        }
        else
        {
            List<SequenceFlow> inputFlowsWithTokens = new ArrayList<>();

            for (SequenceFlow inputFlow : inputSequenceFlows)
            {
                if (inputFlow.hasTokens())
                {
                    inputFlowsWithTokens.add(inputFlow);
                }
            }

            SequenceFlow flow = selectFlowToMoveFrom(inputFlowsWithTokens);
            Token consumedToken = flow.consumeToken();

            if (!flow.hasTokens())
            {
                movementResult.addEmptiedTokenable(flow);
            }

            return consumedToken;

        }
    }

    protected <T extends SequenceFlow> T selectFlowToMoveFrom(List<T> inputFlowsWithTokens)
    {
        int index = random.nextInt(inputFlowsWithTokens.size());
        return inputFlowsWithTokens.get(index);
    }

    @Override
    public boolean checkAvailability()
    {
        for (MessageFlow inputMessageFlow : inputMessageFlows)
        {
            if (!inputMessageFlow.hasTokens())
            {
                return false;
            }
        }


        if (inputSequenceFlows.isEmpty())
        {
            return true;
        }

        for (SequenceFlow inputFlow : inputSequenceFlows)
        {
            if (inputFlow.hasTokens())
            {
                return true;
            }
        }

        return false;
    }

    protected List<? extends SequenceFlow> getInputSequenceFlows()
    {
        return inputSequenceFlows;
    }

    protected List<? extends SequenceFlow> getOutputSequenceFlows()
    {
        return outputSequenceFlows;
    }

    protected List<? extends MessageFlow> getInputMessageFlows()
    {
        return inputMessageFlows;
    }

    protected List<? extends MessageFlow> getOutputMessageFlows()
    {
        return outputMessageFlows;
    }

    public org.processmining.models.graphbased.directed.bpmn.elements.Activity getActualActivity()
    {
        return actualActivity;
    }

    @Override
    public String toString()
    {
        return "Loggable version of " + actualActivity;
    }

    public static class ActivityBuilder extends AbstractNodeBuilder<
            Activity, org.processmining.models.graphbased.directed.bpmn.elements.Activity,
            SimpleSequenceFlow, SimpleMessageFlow>
    {
        public ActivityBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity)
        {
            super(actualActivity);

            if (actualActivity == null)
            {
                throw new NullPointerException("Activity cannot be null");
            }
        }


        @Override
        public Activity build()
        {
            return new Activity(
                    actualNode, parentSubProcess,
                    inputSequenceFlows, outputSequenceFlows,
                    inputMessageFlows, outputMessageFlows);
        }

    }
}
