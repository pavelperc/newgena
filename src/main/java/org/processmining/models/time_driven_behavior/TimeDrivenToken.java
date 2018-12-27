package org.processmining.models.time_driven_behavior;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.TokenWithTime;
import org.processmining.models.abstract_net_representation.AbstractPetriNode;
import org.processmining.models.organizational_extension.Resource;

/**
 * Created by Ivan Shugurov on 28.10.2014.
 */
public class TimeDrivenToken extends TokenWithTime
{
    private final AbstractPetriNode node;
    private final Resource resource;

    public TimeDrivenToken(AbstractPetriNode node)
    {
        this(node, 0);
    }

    public TimeDrivenToken(AbstractPetriNode node, long timestamp)
    {
        this(node, null, timestamp);
    }

    public TimeDrivenToken(AbstractPetriNode node, Resource resource, long timestamp)
    {
        super(timestamp);

        if (timestamp < 0)
        {
            throw new IllegalArgumentException("Timestamp cannot be negative");
        }
        this.node = node;
        this.resource = resource;
    }

    public Resource getResource()
    {
        return resource;
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        if (getNode() instanceof TimeDrivenTransition)    //TODO это очень грустно(
        {
            TimeDrivenTransition transition = (TimeDrivenTransition) getNode();
            return transition.moveInternalToken(trace, this);
        }
        else
        {
            throw new IllegalStateException();//TODO а так вообще должно быть?
        }
    }

    public TimeDrivenToken copyTokenWithNewTimestamp(long newTimestamp)
    {
        return new TimeDrivenToken(getNode(), getResource(), newTimestamp);
    }

    public AbstractPetriNode getNode()
    {
        return node;
    }

    @Override
    public String toString()
    {
        return "Token{" +
                "node=" + node +
                '}';
    }
}
