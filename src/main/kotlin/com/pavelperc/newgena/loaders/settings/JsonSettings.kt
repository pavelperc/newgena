package com.pavelperc.newgena.loaders.settings

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pavelperc.newgena.utils.propertyinitializers.ExclusiveBoolean
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeInt
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeLong
import com.pavelperc.newgena.utils.propertyinitializers.RangeInt
import org.processmining.models.GenerationDescription
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.NoiseEvent
import java.time.Instant
import java.util.*


/** Json representation of all generation settings.
 * Represents all adjustable(!) parameters.
 * Should be json serializable, all properties are mutable.
 * This class is not used during generation, but can be converted in [GenerationDescription] class via [JsonSettingsBuilder].
 */
class JsonSettings {
    class Marking {
        var initialPlaceIds = mutableListOf<String>()
        var finalPlaceIds = mutableListOf<String>()
    }
    
    class Noise {
        var noiseLevel by RangeInt(5, 1..100)
//            set(value) {
//                if (value !in 1..100)
//                    throw IllegalArgumentException("Precondition violated in NoiseDescription. Unaccepted noise level")
//                field = value
//            }
        
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

// ----------------------------- VARIABLES: ---------------------------
    
    var petrinetFile = "petrinet.pnml"
    
    var marking = Marking()
    var numberOfLogs by NonNegativeInt(5)
    var numberOfTraces by NonNegativeInt(10)
    
    var maxNumberOfSteps by NonNegativeInt(100)
    
    var isRemovingEmptyTraces = true
    
    var isRemovingUnfinishedTraces = true
    
    var isUsingNoise by ExclusiveBoolean(::isUsingStaticPriorities)
    
    var noiseDescription: Noise? = Noise()
    
    var isUsingStaticPriorities: Boolean by ExclusiveBoolean(::isUsingStaticPriorities)
    
    var staticPriorities: StaticPriorities? = StaticPriorities()
    
    var isUsingTime by ExclusiveBoolean(::isUsingStaticPriorities)
//        set(value) {
//            checkExclusive(value to "isUsingTime", isUsingStaticPriorities to "isUsingStaticPriorities")
//        }
    
    var timeDescription: JsonTimeDescription? = JsonTimeDescription()
    
    companion object {}
}

/** Json representation of TimeDescription */
class JsonTimeDescription {
    
    data class DelayWithDeviation(
            var delay: Long,
            var deviation: Long
    ) {
        fun toPair(): Pair<Long, Long> = delay to deviation
    }
    
    class TimeDrivenNoise {
        var isUsingTimestampNoise: Boolean = true
        var isUsingLifecycleNoise: Boolean = true
        var isUsingTimeGranularity: Boolean = true
        var maxTimestampDeviationSeconds: Int = 0
        var granularityType: GranularityTypes = GranularityTypes.MINUTES_5
    }
    
    var isUsingResources = false
    var isUsingLifecycle = false
    
    var isUsingComplexResourceSettings: Boolean = true
    var isUsingSynchronizationOnResources: Boolean = true
    
    var minimumIntervalBetweenActions by NonNegativeInt(10)
    var maximumIntervalBetweenActions by NonNegativeInt(20)
    
    var isSeparatingStartAndFinish: Boolean = true
    
    var simplifiedResources = mutableListOf<JsonResources.Resource>()
    var resourceGroups = mutableListOf<JsonResources.Group>()
    
    var transitionIdsToDelays = mutableMapOf<String, DelayWithDeviation>()
    
    var generationStart: Instant = Instant.now()
    
    // will be converted to resourceMapping
    var transitionIdsToResources = mutableMapOf<String, JsonResources.ResourceMapping>()
    
    var timeDrivenNoise: TimeDrivenNoise? = TimeDrivenNoise()
}

/** Json representation of resources */
object JsonResources {
    data class Role(
            var name: String = "role"
    ) {
        var resources = mutableListOf<Resource>(Resource())
    }
    
    data class Resource(
            var name: String = "resource"
    ) {
        var willBeFreed by NonNegativeLong(0L)
        var minDelayBetweenActionsMillis by NonNegativeLong(15 * 60 * 1000)
        var maxDelayBetweenActionsMillis by NonNegativeLong(20 * 60 * 1000)
    }
    
    data class Group(
            var name: String = "group"
    ) {
        var roles = mutableListOf<Role>(Role())
    }
    
    class ResourceMapping {
        data class FullResourceName(
                var groupName: String,
                var roleName: String,
                var resourceName: String
        )
        
        var fullResourceNames = mutableListOf<FullResourceName>()
        
        var simplifiedResourceNames = mutableListOf<String>()
    }
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