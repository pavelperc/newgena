package org.processmining.models.base_bpmn;

import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Ivan Shugurov on 21.12.2014.
 */
public class SimpleSequenceFlow implements SequenceFlow<Token>
{
    private final Flow flow;
    private Queue<Token> tokens = new LinkedList<Token>();

    public SimpleSequenceFlow(Flow flow)
    {
        if (flow == null)
        {
            throw new NullPointerException("Flow cannot be null");
        }

        this.flow = flow;
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
    public void addToken(Token token)
    {
        if (token == null)
        {
            throw new NullPointerException("Token cannot be equal to null");
        }
        tokens.add(token);
    }

    @Override
    public Token consumeToken()
    {
        return tokens.remove();
    }

    @Override
    public void removeToken(Token token)
    {
        tokens.remove(token);
    }

    @Override
    public void removeAllTokens()
    {
        tokens.clear();
    }

    @Override
    public Token peekToken()
    {
        return tokens.peek();
    }

    @Override
    public Flow getFlow()
    {
        return flow;
    }

}
