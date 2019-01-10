package org.processmining.models.time_driven_behavior;

import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;

import java.util.PriorityQueue;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class TimeDrivenPlace extends Place<TimeDrivenToken>
{
    public TimeDrivenPlace(org.processmining.models.graphbased.directed.petrinet.elements.Place node, TimeDrivenGenerationDescription generationDescription)
    {
        super(node, generationDescription);
        setTokens(new PriorityQueue<TimeDrivenToken>());
    }

    @Override
    public void addToken(TimeDrivenToken token)
    {
        if (token == null)
        {
            throw new NullPointerException("Token cannot be null");
        }
        getTokens().add(token);
    }


    public long getLowestTimestamp()
    {
        return getTokens().peek().getTimestamp();
    }
}
