package org.processmining.models.abstract_net_representation

import org.processmining.models.GenerationDescription
import org.processmining.models.Tokenable

import java.util.LinkedList
import java.util.Queue

/**
 * @author Ivan Shugurov
 * Created  02.12.2013
 */
open class Place<T : Token>(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Place,
        generationDescription: GenerationDescription
) : AbstractPetriNode(node, generationDescription), Tokenable<T> {
    
    protected open val tokens: Queue<T> = LinkedList()
    
    override val numberOfTokens: Int
        get() = tokens.size
    
    override fun hasTokens() = !tokens.isEmpty()
    
    fun hasTokens(amount: Int) = numberOfTokens >= amount
    
    
    override fun addToken(token: T) {
        tokens.add(token)
    }
    
    override fun consumeToken(): T {
        return tokens.remove()
    }
    
    override fun removeToken(token: T) {
        tokens.remove(token)
    }
    
    override fun removeAllTokens() {
        tokens.clear()
    }
    
    override fun peekToken(): T {
        return tokens.peek()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Place<*>
        
        return other.node == node
    }
    
    override fun hashCode(): Int {
        return node.hashCode() + 13
    }
}
