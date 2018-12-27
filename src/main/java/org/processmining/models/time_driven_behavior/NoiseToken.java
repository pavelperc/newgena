package org.processmining.models.time_driven_behavior;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.utils.TimeDrivenLoggingSingleton;

/**
 * @author Ivan Shugurov
 *         Created  30.07.2014
 */
public class NoiseToken extends TimeDrivenToken
{
    private final Object modelActivity;


    public NoiseToken(Object modelActivity, Resource resource, long timestamp)
    {
        super(null, resource, timestamp);
        this.modelActivity = modelActivity;
    }

    @Override
    public TimeDrivenToken copyTokenWithNewTimestamp(long newTimestamp)
    {
        return new NoiseToken(modelActivity, getResource(), newTimestamp);
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult<TimeDrivenToken> movementResult = new MovementResult<TimeDrivenToken>();
        movementResult.addConsumedExtraToken(this);
        registerEvent(trace);
        return movementResult;
    }

    private void registerEvent(XTrace trace)
    {
        if (getResource() == null)
        {
            TimeDrivenLoggingSingleton.timeDrivenInstance().log(trace, modelActivity, getTimestamp(), true);
        }
        else
        {
            TimeDrivenLoggingSingleton.timeDrivenInstance().logCompleteEventWithResource(trace, modelActivity, getResource(), getTimestamp());
        }
    }

}
