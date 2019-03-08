package org.processmining.models.base_bpmn;

import org.processmining.models.Movable;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;

import java.util.*;

/**
 * Created by Ivan on 16.09.2015.
 */
public abstract class AbstractSubProcess<T extends Token, SF extends SequenceFlow<T>, MF extends MessageFlow<T>, SP extends AbstractSubProcess> implements Movable
{
    private static Random random = new Random();
    private final Collection<? extends MF> inputMessageFlows;
    private final Collection<? extends MF> outputMessageFlows;
    private org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess;
    private Collection<? extends SF> inputFlows;
    private Collection<? extends SF> outputFlows;
    private Collection<? extends SF> internalSequenceFlows;
    private Collection<? extends SF> dummyFlowsToFrontSubProcessElements;   //TODO elements? not only one? it it real?
    private Collection<? extends SF> cancelOutputFlows;
    private boolean inProgress;
    private SP parentSubProcess;
    private Set<SP> nestedSubProcesses = new HashSet<>();

    @SuppressWarnings("unchecked")
    protected AbstractSubProcess(
            org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess,
            SP parentSubProcess,
            Collection<? extends SF> inputFlows,
            Collection<? extends SF> outputFlows,
            Collection<? extends SF> internalSequenceFlows,
            Collection<? extends SF> dummyFlowsToFrontSubProcessElements,
            Collection<? extends SF> cancelOutputFlows,
            Collection<? extends MF> inputMessageFlows,
            Collection<? extends MF> outputMessageFlows)
    {
        this.actualSubProcess = actualSubProcess;
        this.inputFlows = inputFlows;
        this.outputFlows = outputFlows;
        this.internalSequenceFlows = internalSequenceFlows;
        this.dummyFlowsToFrontSubProcessElements = dummyFlowsToFrontSubProcessElements;
        this.cancelOutputFlows = cancelOutputFlows;
        this.inputMessageFlows = inputMessageFlows;
        this.outputMessageFlows = outputMessageFlows;
        this.parentSubProcess = parentSubProcess;

        if (parentSubProcess != null)
        {
            parentSubProcess.addNestedSubProcess(this);
        }
    }

    public SP getParentSubProcess()
    {
        return parentSubProcess;
    }

    public Set<SP> getNestedSubProcesses()
    {
        return nestedSubProcesses;
    }

    protected Collection<? extends SF> getInternalSequenceFlows()
    {
        return internalSequenceFlows;
    }

    @Override
    public boolean checkAvailability()
    {
        if (inProgress)
        {
            return false;
        }

        for (MF inputMessageFlow : inputMessageFlows)
        {
            if (!inputMessageFlow.hasTokens())
            {
                return false;
            }
        }

        for (SequenceFlow inFlow : inputFlows)
        {
            if (inFlow.hasTokens())
            {
                return true;
            }
        }

        return false;
    }

    public void stopSubProcess()
    {
        inProgress = false;
    }

    protected String getLane()
    {
        Swimlane lane =  actualSubProcess.getParentLane();

        if (lane == null)
        {
            return "";
        }
        else
        {
            return lane.toString();
        }
    }

    /**
     * Checks if the sub-process terminated. if so, adds tokens to output sequence flows
     * @param normalTermination
     */
    protected MovementResult updateState(boolean normalTermination)
    {
        MovementResult movementResult = new MovementResult(false);

        if (!hasInternalTokens() && !nestedSubProcessesAreInProgress())
        {
            inProgress = false;

            if (normalTermination)
            {
                produceOutput(movementResult);
            }

            if (parentSubProcess != null)
            {
                parentSubProcess.updateState(true);
            }
        }

        return movementResult;
    }

    protected boolean nestedSubProcessesAreInProgress()
    {
        for (SP nestedSubProcess : nestedSubProcesses)
        {
            if (nestedSubProcess.isInProgress())
            {
                return true;
            }
        }

        return false;
    }

    protected void produceOutput(MovementResult movementResult)
    {
        for (SequenceFlow<T> outFlow : outputFlows)
        {
            if (!outFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outFlow);
            }

            outFlow.addToken(createToken());
        }

        for (MF outputMessageFlow : outputMessageFlows)
        {
            if (!outputMessageFlow.hasTokens())
            {
                movementResult.addFilledTokenables(outputMessageFlow);
            }

            outputMessageFlow.addToken(createToken());
        }

        inProgress = false;
    }

    @SuppressWarnings("unchecked")
    protected MovementResult cancelSubProcess()
    {
        MovementResult movementResult = deleteTokens();
        //TODO is it correct behavior? What do we say in the article?
        for (SF cancelOutputFlow : cancelOutputFlows)
        {
            if (!cancelOutputFlow.hasTokens())
            {
                movementResult.addFilledTokenables(cancelOutputFlow);
            }

            cancelOutputFlow.addToken(createToken());
        }

        inProgress = false;

        if (parentSubProcess != null)
        {
            parentSubProcess.updateState(false);
        }

        return movementResult;
    }

    protected MovementResult deleteTokens()
    {
        inProgress = false;

        MovementResult movementResult = new MovementResult();

        for (SequenceFlow internalSequenceFlow : internalSequenceFlows)
        {
            if (internalSequenceFlow.hasTokens())
            {
                movementResult.addEmptiedTokenable(internalSequenceFlow);
                internalSequenceFlow.removeAllTokens();
            }
        }

        for (SequenceFlow flow : dummyFlowsToFrontSubProcessElements)
        {
            if (flow.hasTokens())
            {
                movementResult.addEmptiedTokenable(flow);
                flow.removeAllTokens();
            }
        }

        for (SP nestedSubProcess : nestedSubProcesses)
        {
            if (nestedSubProcess.isInProgress())
            {
                MovementResult nestedResult = nestedSubProcess.deleteTokens();
                movementResult.addAllEmptiedTokenables(nestedResult.getEmptiedTokenables());
                movementResult.addAllFilledTokenables(nestedResult.getFilledTokenables());
            }
        }

        return movementResult;
    }

    protected abstract T createToken();


    public boolean hasInternalTokens()
    {
        for (SequenceFlow inFlow : internalSequenceFlows)
        {
            if (inFlow.hasTokens())
            {
                return true;
            }
        }
        return false;
    }

    protected void addNestedSubProcess(SP subProcess)
    {
        if (nestedSubProcesses == null)
        {
            throw new NullPointerException("Nested sub process cannot be equal to null");
        }

        nestedSubProcesses.add(subProcess);
    }

    protected <E> E pickRandomElement(List<? extends E> elements)
    {
        int index = random.nextInt(elements.size());
        return elements.get(index);
    }

    //TODO   ���������� ���� private. ������� private?
    protected List<? extends SF> getSequenceFlowsWithTokens()
    {
        List<SF> inputFlowsWithTokens = new ArrayList<SF>();

        for (SF inputFlow : inputFlows)
        {
            if (inputFlow.hasTokens())
            {
                inputFlowsWithTokens.add(inputFlow);
            }
        }
        return inputFlowsWithTokens;
    }

    public boolean isInProgress()
    {
        return inProgress;
    }

    protected void setInProgress(boolean inProgress)
    {
        this.inProgress = inProgress;
    }

    protected Collection<? extends MF> getInputMessageFlows()
    {
        return inputMessageFlows;
    }

    protected Collection<? extends MF> getOutputMessageFlows()
    {
        return outputMessageFlows;
    }

    protected Collection<? extends SF> getInputFlows()
    {
        return inputFlows;
    }

    protected Collection<? extends SF> getOutputFlows()
    {
        return outputFlows;
    }

    protected Collection<? extends SF> getDummyFlowsToFrontSubProcessElements()
    {
        return dummyFlowsToFrontSubProcessElements;
    }

    protected Collection<? extends SF> getCancelOutputFlows()
    {
        return cancelOutputFlows;
    }

    public String getLabel()
    {
        return actualSubProcess.getLabel();
    }

    @Override
    public String toString()
    {
        return "Loggable version of " + actualSubProcess;
    }
}
