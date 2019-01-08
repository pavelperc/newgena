package org.processmining.models

import org.processmining.models.abstract_net_representation.Token

/**
 * Created by Ivan Shugurov on 27.10.2014.
 */
interface Tokenable<T : Token> {
    val numberOfTokens: Int
    
    fun hasTokens(): Boolean
    
    fun addToken(token: T)
    
    fun consumeToken(): T
    
    fun removeToken(token: T)
    
    fun removeAllTokens()
    
    fun peekToken(): T
    
}
