package org.processmining.models.bpmn_with_time;

import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.SequenceFlow;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Ivan on 25.08.2015.
 */
public class SequenceFlowWithTime implements SequenceFlow<TokenWithTime>
{
    private final Flow flow;
    private Queue<TokenWithTime> tokens = new PriorityQueue<TokenWithTime>();

    public SequenceFlowWithTime(Flow flow)
    {
        if (flow == null)
        {
            throw new NullPointerException("Flow cannot be null");
        }

        this.flow = flow;
    }

    @Override
    public Flow getFlow()
    {
        return flow;
    }

    @Override
    public int getNumberOfTokens()
    {
        return tokens.size();
    }

    @Override
    public boolean hasTokens()
    {
        return !tokens.isEmpty();
    }

    @Override
    public void addToken(TokenWithTime token)
    {
        tokens.add(token);
    }

    @Override
    public TokenWithTime consumeToken()
    {
        return tokens.poll();
    }

    @Override
    public void removeToken(TokenWithTime token)
    {
        tokens.remove(token);
    }

    @Override
    public void removeAllTokens()
    {
         tokens.clear();
    }

    @Override
    public TokenWithTime peekToken()
    {
        return tokens.peek();
    }
}
