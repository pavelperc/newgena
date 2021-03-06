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
// made as extensions because of @JvmDefault restrictions


fun <T : Token> Tokenable<T>.consumeAllTokens(): List<T> {
    return consumeTokens(numberOfTokens)
}

fun <T : Token> Tokenable<T>.consumeTokens(amount: Int): List<T> {
    return List(amount) { consumeToken() }
}

fun <T : Token> Tokenable<T>.addTokens(tokens: List<T>) {
    tokens.forEach { addToken(it) }
}
