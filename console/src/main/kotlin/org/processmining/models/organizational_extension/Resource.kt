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
        val role: Role? = null,
        val minDelayBetweenActions: Long = DEFAULT_MIN_DELAY_BETWEEN_ACTIONS,
        val maxDelayBetweenActions: Long = DEFAULT_MAX_DELAY_BETWEEN_ACTIONS
) : Comparable<Resource>        //TODO неправильно рабоатет с одинаковыми именами ресурсов
{
    companion object {
        /** 15 minutes in milliseconds */
        val DEFAULT_MAX_DELAY_BETWEEN_ACTIONS = TimeUnit.MINUTES.toMillis(20)
        /** 20 minutes in milliseconds */
        val DEFAULT_MIN_DELAY_BETWEEN_ACTIONS = TimeUnit.MINUTES.toMillis(15)
        
        fun simplified(name: String) = Resource(name, null, 0, 0)
    }
    
    /** Group is taken from the role. */
    val group = role?.group
    
    
    init {
        if (minDelayBetweenActions > maxDelayBetweenActions) {
            throw IllegalArgumentException("Precondition violated in $this. minDelayBetweenActions > maxDelayBetweenACtions.")
        }
    }
    
    /** Name in format name:role:group .*/
    val fullName: String
        get() = "$name:${role?.name}:${group?.name}"
    
    
    /** Time in milliseconds, when the resource will be freed. */
    var willBeFreed: Long = 0
        private set
    
    /** Is vacant. (We can use this resource.) */
    var isIdle = true
    
    
    /** Set [Resource.willBeFreed] and add a delay, if the [willBeFreed] not equals zero. */
    fun setTime(willBeFreed: Long)
    {
        this.willBeFreed = willBeFreed
        if (willBeFreed != 0L) {
            addDelay()
        }
    }
    
    private fun addDelay() {
        willBeFreed += Random.nextLong(minDelayBetweenActions..maxDelayBetweenActions)
    }
    
    override fun toString() = name
    
    override fun compareTo(other: Resource) = this.name.compareTo(other.name)
}
