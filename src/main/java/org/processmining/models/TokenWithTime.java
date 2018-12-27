package org.processmining.models;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.bpmn_with_time.MovableWithTime;

/**
 * Created by Ivan on 12.08.2015.
 */
public class TokenWithTime extends Token implements Comparable<TokenWithTime>, MovableWithTime
{
    private long timestamp;
    private NodeCallback callback;

    public TokenWithTime(long timestamp)
    {
        this(null, timestamp);
    }

    public TokenWithTime(NodeCallback callback, long timestamp)
    {
        this.callback = callback;
        this.timestamp = timestamp;
    }

    public NodeCallback getCallback()
    {
        return callback;
    }

    public void setCallback(NodeCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public int compareTo(TokenWithTime anotherToken)
    {
        return Long.compare(timestamp, anotherToken.timestamp);
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        if (callback == null)
        {
            return null;
        }
        else
        {
            return callback.move(trace);
        }
    }
}
