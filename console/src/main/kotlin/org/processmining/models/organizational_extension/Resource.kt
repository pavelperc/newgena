package org.processmining.models.organizational_extension

import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.random.nextULong

/**
 * @author Ivan Shugurov
 * Created on 02.04.2014
 */
class Resource constructor(
        val name: String,
        willBeFreed: Long = 0L,
        val minDelayBetweenActions: Long = DEFAULT_MIN_DELAY_BETWEEN_ACTIONS,
        val maxDelayBetweenActions: Long = DEFAULT_MAX_DELAY_BETWEEN_ACTIONS,
        val group: Group? = null,
        val role: Role? = null
) : Comparable<Resource>        //TODO неправильно рабоатет с одинаковыми именами ресурсов
{
    init {
        if ((role == null) xor (group == null))
            throw IllegalArgumentException("Error in Resource.init(): role and group should be both null or not.")
        
        
        if (role?.group != group) {
            throw IllegalArgumentException("Precondition violated in Resource.init(). Incorrect role")
        }
        
        if (willBeFreed != 0L) {
            addDelay()
        }
    }
    
    var willBeFreed = willBeFreed
        private set
    
    var isIdle = true
    
    /** set [willBeFreed] and add a delay */
    fun setTime(willBeFreed: Long)//TODO подумать получше над названием
    {
        this.willBeFreed = willBeFreed
        if (willBeFreed != 0L) {
            addDelay()
        }
    }
    
//    fun relocate(newGroup: Group, newRole: Role) {
//        // TODO: logic?
//        if (newGroup != newRole.group) {
//            group?.removeResource(this)
//        }
//        group = newGroup
//        role?.resources?.remove(this)
//        role = newRole
//        newRole.resources.add(this)
//    }
    
    private fun addDelay() {
        willBeFreed += Random.nextLong(minDelayBetweenActions..maxDelayBetweenActions)
    }
    
    override fun toString() = "Resource($name)"
    
    override fun compareTo(other: Resource) = this.name.compareTo(other.name)
    
//    fun removeResource() {
//        group?.removeResource(this)
//    }
    
//    fun setDelayBetweenActions(minDelay: Long, maxDelay: Long) {
//        minDelayBetweenActions = minDelay
//        maxDelayBetweenActions = maxDelay
//    }
    
    companion object {
        /** 15 minutes in milliseconds */
        val DEFAULT_MAX_DELAY_BETWEEN_ACTIONS = TimeUnit.MINUTES.toMillis(20)
        /** 20 minutes in milliseconds */
        val DEFAULT_MIN_DELAY_BETWEEN_ACTIONS = TimeUnit.MINUTES.toMillis(15)
    }
}
