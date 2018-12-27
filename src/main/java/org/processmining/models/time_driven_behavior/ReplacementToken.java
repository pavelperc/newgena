package org.processmining.models.time_driven_behavior;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.utils.TimeDrivenLoggingSingleton;

import java.util.Random;

/**
 * Created by Ivan Shugurov on 09.10.2014.
 */
public class ReplacementToken extends TimeDrivenToken
{
    private static final Random random = new Random();
    private final Object recorderActivity;
    private final TimeDrivenGenerationDescription description;
    private final long possibleTimeVariation;

    public ReplacementToken(TimeDrivenGenerationDescription description, TimeDrivenTransition realNode, Object recordedActivity, Resource resource, long timestamp)
    {
        super(realNode, resource, timestamp);
        if (recordedActivity == null)
        {
            throw new IllegalArgumentException("Recorded activity cannot be null");
        }
        if (description == null)
        {
            throw new IllegalArgumentException("Generation description cannot be null");
        }
        this.recorderActivity = recordedActivity;
        this.description = description;
        possibleTimeVariation = description.getMaximumIntervalBetweenActions() - description.getMinimumIntervalBetweenActions();
    }


    @Override
    public ReplacementToken copyTokenWithNewTimestamp(long newTimestamp)
    {
        return new ReplacementToken(description, getNode(), recorderActivity, getResource(), newTimestamp);
    }

    @Override
    public TimeDrivenTransition getNode()
    {
        return (TimeDrivenTransition) super.getNode();
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult<ReplacementToken> movementResult = new MovementResult<ReplacementToken>();
        movementResult.addConsumedExtraToken(this);
        registerEvent(trace);
        TimeDrivenTransition node = getNode();
        TimeDrivenPlace[] outputPlaces = node.getOutputPlaces();
        for (TimeDrivenPlace outPlace : outputPlaces)
        {
            addToken(outPlace);
        }
        return movementResult;
    }

    protected void addToken(Place<TimeDrivenToken> outPlace)
    {
        long timeBetweenActions = description.getMinimumIntervalBetweenActions() + ((long) (random.nextDouble() * (possibleTimeVariation + 1)));

        TimeDrivenToken token = new TimeDrivenToken(outPlace, getTimestamp() + timeBetweenActions * 1000);
        outPlace.addToken(token);
    }

    private void registerEvent(XTrace trace)
    {
        if (getResource() == null)
        {
            TimeDrivenLoggingSingleton.timeDrivenInstance().log(trace, recorderActivity, getTimestamp(), true);
        }
        else
        {
            TimeDrivenLoggingSingleton.timeDrivenInstance().logCompleteEventWithResource(trace, recorderActivity, getResource(), getTimestamp());
        }
    }
}
