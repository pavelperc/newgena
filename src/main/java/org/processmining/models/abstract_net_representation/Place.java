package org.processmining.models.abstract_net_representation;

import org.processmining.models.GenerationDescription;
import org.processmining.models.Tokenable;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Ivan Shugurov
 *         Created  02.12.2013
 */
public class Place<T extends Token> extends AbstractPetriNode implements Tokenable<T>
{
    protected Queue<T> tokens = new LinkedList<T>();

    public Place(
            org.processmining.models.graphbased.directed.petrinet.elements.Place node,
            GenerationDescription generationDescription
    )
    {
        super(node, generationDescription);
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
    public void addToken(T token)
    {
        if (token == null)
        {
            throw new NullPointerException("Token cannot be null");
        }
        tokens.add(token);
    }

    @Override
    public T consumeToken()
    {
        return tokens.remove();
    }

    @Override
    public void removeToken(T token)
    {
        tokens.remove(token);
    }

    @Override
    public void removeAllTokens()
    {
        tokens.clear();
    }

    @Override
    public T peekToken()
    {
        return tokens.peek();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Place))
        {
            return false;
        }
        Place place = (Place) o;
        return place.getNode().equals(getNode());
    }

    @Override
    public int hashCode()
    {
        return getNode().hashCode() + 13;
    }

}
