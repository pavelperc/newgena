package org.processmining.models;

/**
 * Created by Ivan Shugurov on 25.12.2014.
 */
public class AssessedMovementResult
{
    private final boolean replayCompleted;
    private final boolean traceEligibleForAddingToLog;

    public AssessedMovementResult(boolean replayCompleted, boolean traceEligibleForAddingToLog)
    {
        this.replayCompleted = replayCompleted;
        this.traceEligibleForAddingToLog = traceEligibleForAddingToLog;
    }

    public boolean isTraceEligibleForAddingToLog()
    {
        return traceEligibleForAddingToLog;
    }

    public boolean isReplayCompleted()
    {
        return replayCompleted;
    }
}
