package org.processmining.models;

import org.processmining.models.abstract_net_representation.Token;

/**
 * Created by Ivan Shugurov on 27.10.2014.
 */
public interface Tokenable<T extends Token>
{
    int getNumberOfTokens();

    boolean hasTokens();

    void addToken(T token);

    T consumeToken();

    void removeToken(T token);

    void removeAllTokens();

    T peekToken();

}
