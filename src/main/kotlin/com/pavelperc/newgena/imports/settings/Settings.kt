package com.pavelperc.newgena.imports.settings

import org.processmining.models.GenerationDescription
import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.time_driven_behavior.ResourceMapping
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.util.*


/** All adjustable(!) parameters.
 * Should be json serializable, all properties are mutable.
 * This class is not used during generation, but can be converted in [GenerationDescription] class.
 */

@kotlin.ExperimentalUnsignedTypes
class Settings {
    class Marking {
        var initialPlaceIds = mutableListOf<String>()
        var finalPlaceIds = mutableListOf<String>()
    }
    
    class Noise {
        var noiseLevel = 5
            set(value) {
                if (value !in 1..100)
                    throw IllegalArgumentException("Precondition violated in NoiseDescription. Unaccepted noise level")
                field = value
            }
        
        var isSkippingTransitions = true
        
        var isUsingExternalTransitions = true
        var isUsingInternalTransitions = true
        
        var internalTransitionIds = mutableSetOf<String>()
        var existingNoiseEvents = mutableListOf(NoiseEvent("NoiseEvent"))
    }
    
    class StaticPriorities {
        var maxPriority: Int = 1
        /** Ids with larger numbers go first. Default priority is 1.*/
        var transitionIdsToPriorities = mutableMapOf<String, Int>()
    }
    
    class TimeDescription {
        
        data class DelayWithDeviation(
                var delay: Long,
                var deviation: Long
        )
        
        class TimeDrivenNoise {
            
        }
        
        var isUsingComplexResourceSettings: Boolean = true
        var isUsingSynchronizationOnResources: Boolean = true
        
        var minimumIntervalBetweenActions = 10u
        var maximumIntervalBetweenActions = 20u
        
        var isSeparatingStartAndFinish: Boolean = true
        
        var simplifiedResources = mutableListOf<Resource>()
        
        var transitionIdsToDelays = mutableMapOf<String, DelayWithDeviation>()
        
        var generationStart: LocalDateTime = LocalDateTime.now()
        
        val resourceMapping = mutableMapOf<Any, ResourceMapping>()
        val resourceGroups = mutableListOf<Group>()
        
        
    }
    
    var marking = Marking()
    var numberOfLogs = 5u
    var numberOfTraces = 10u
    var maxNumberOfSteps = 100u

//    var isUsingTime = false
//    var isUsingResources = false
//    var isUsingLifecycle = false
//    var isRemovingEmptyTraces = true
    
    var isRemovingUnfinishedTraces = true
    
    
    var isUsingNoise = false
        set(value) {
            checkExclusive(value to "isUsingNoise",
                    isUsingStaticPriorities to "isUsingStaticPriorities")
            field = value
        }
    
    private fun checkExclusive(vararg paramToNames: Pair<Boolean, String>) {
        paramToNames
                .filter { it.first } // true params
                .map { it.second } // select names
                .apply {
                    if (size > 1)
                        throw IllegalStateException("Parameters ${joinToString(", ")} are exclusive.")
                }
    }
    
    var noiseDescription: Noise? = Noise()
    
    
    var isUsingStaticPriorities = false
        set(value) {
            checkExclusive(value to "isUsingStaticPriorities",
                    isUsingNoise to "isUsingNoise")
            field = value
        }
    
    var staticPriorities: StaticPriorities? = StaticPriorities()
    
    
}

/** Injects purChecker for put and putAll operations */
class CheckerMap<K, V>(val checkerOnPut: (K, V) -> Unit) : LinkedHashMap<K, V>() {
    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach { (k, v) -> checkerOnPut(k, v) }
        super.putAll(from)
    }
    
    override fun put(key: K, value: V): V? {
        checkerOnPut(key, value)
        return super.put(key, value)
    }
}