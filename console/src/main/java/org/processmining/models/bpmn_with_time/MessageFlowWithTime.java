package org.processmining.models.bpmn_with_time;

import org.processmining.models.TokenWithTime;
import org.processmining.models.base_bpmn.MessageFlow;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Ivan on 03.09.2015.
 */
public class MessageFlowWithTime implements MessageFlow<TokenWithTime>
{
    private org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow actualFlow;
    private Queue<TokenWithTime> tokens = new PriorityQueue<TokenWithTime>();

    public MessageFlowWithTime(org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow actualFlow)
    {
        this.actualFlow = actualFlow;
    }

    @Override
    public org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow getFlow()
    {
        return actualFlow;
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
        return tokens.remove();
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
