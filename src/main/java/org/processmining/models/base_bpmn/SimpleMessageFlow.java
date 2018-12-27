package org.processmining.models.base_bpmn;

import org.processmining.models.abstract_net_representation.Token;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Ivan on 08.04.2015.
 */
public class SimpleMessageFlow implements MessageFlow<Token>
{
    private org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow actualFlow;
    private Queue<Token> tokens = new LinkedList<Token>();

    public SimpleMessageFlow(org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow actualFlow)
    {
        if (actualFlow == null)
        {
            throw new NullPointerException("Token cannot be equal to null");
        }

        this.actualFlow = actualFlow;
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

    public org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow getFlow()
    {
        return actualFlow;
    }
}
