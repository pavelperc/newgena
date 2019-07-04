package com.pavelperc.newgena.loaders.settings

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import org.processmining.models.GenerationDescription
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.time_driven_behavior.ResourceMapping
import java.time.Instant
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor


@Serializable
data class SettingsInfo(
        @Required val type: String,
        @Required val version: String
)


/** Json representation of all generation settings.
 * Represents all adjustable parameters.
 * Should be json serializable, all properties are mutable.
 * This class is not used during generation, but can be converted in [GenerationDescription] class via [JsonSettingsBuilder].
 */
@Serializable
class JsonSettings {
    companion object {
        const val LAST_SETTINGS_VERSION = "0.3"
    }
    
    @Required
    val settingsInfo = SettingsInfo("petrinet", LAST_SETTINGS_VERSION)
    
    @Required
    var petrinetSetup = JsonPetrinetSetup()
    @Required
    var outputFolder = "xes-out"
    
    @Required
    var numberOfLogs = 5
    @Required
    var numberOfTraces = 10
    @Required
    var maxNumberOfSteps = 100
    
    @Required
    var isRemovingEmptyTraces = true
    @Required
    var isRemovingUnfinishedTraces = true
    
    @Required
    var isUsingNoise = false
    @Required
    var noiseDescription: JsonNoise = JsonNoise()
    
    @Required
    var isUsingStaticPriorities: Boolean = false
    @Required
    var staticPriorities: JsonStaticPriorities = JsonStaticPriorities()
    
    @Required
    var isUsingTime = false
    @Required
    var timeDescription: JsonTimeDescription = JsonTimeDescription()
    
    
    init {
        check(numberOfLogs >= 0) { "Field numberOfLogs should not be negative." }
        check(numberOfTraces >= 0) { "Field numberOfTraces should not be negative." }
        check(maxNumberOfSteps >= 0) { "Field maxNumberOfSteps should not be negative." }
        
        val defaultInfo = SettingsInfo("petrinet", LAST_SETTINGS_VERSION)
        
        check(settingsInfo == defaultInfo) {
            "Settings info after all migrations should be $defaultInfo, but it is $settingsInfo."
        }
    }
    
    override fun toString() = reflectionToString(this)
}

fun reflectionToString(any: Any) =
        any::class.declaredMemberProperties
                .joinToString(",\n", "${any::class.simpleName}(\n", "\n)") { prop ->
                    "${prop.name}: ${prop.call(any)}".prependIndent()
                }

@Serializable
class JsonMarking() {
    
    @Required
    var initialPlaceIds = mutableMapOf<String, Int>()
    @Required
    var finalPlaceIds = mutableMapOf<String, Int>()
    
    @Required
    var isUsingInitialMarkingFromPnml = false
    
    override fun toString() = reflectionToString(this)
}

@Serializable
class JsonPetrinetSetup {
    
    @Required
    var petrinetFile = "petrinet.pnml"
    @Required
    var marking = JsonMarking()
    @Required
    var inhibitorArcIds: MutableList<String> = mutableListOf()
    @Required
    var resetArcIds: MutableList<String> = mutableListOf()
    
    override fun toString() = reflectionToString(this)
}

@Serializable
class JsonNoise {
    @Required
    var noiseLevel = 5
    
    @Required
    var isSkippingTransitions = true
    
    @Required
    var isUsingExternalTransitions = true
    @Required
    var isUsingInternalTransitions = true
    
    @Required
    var internalTransitionIds = mutableListOf<String>()
    @Required
    var artificialNoiseEvents = mutableListOf<NoiseEvent>()
    
    override fun toString() = reflectionToString(this)
    
    init {
        check(noiseLevel in 1..100) { "Field noiseLevel should be in range 1..100 ." }
    }
}

@Serializable
class JsonStaticPriorities {
    @Required
    var maxPriority: Int = 100 // >= 1
    /** Ids with larger numbers go first. Default priority is 1.*/
    @Required
    var transitionIdsToPriorities = mutableMapOf<String, Int>()
    
    init {
        check(maxPriority >= 1) { "Field maxPriority should be >= 1." }
    }
    
    override fun toString() = reflectionToString(this)
}

@Serializable
class JsonTimeDrivenNoise {
    
    @Required
    var isUsingTimestampNoise = true
    @Required
    var isUsingLifecycleNoise = true
    @Required
    var isUsingTimeGranularity = true
    @Required
    var maxTimestampDeviationSeconds = 0
    @Required
    var granularityType: GranularityTypes = GranularityTypes.MINUTES_5
    
    override fun toString() = reflectionToString(this)
}

/** Json representation of TimeDescription */
@Serializable
class JsonTimeDescription {
    @Serializable
    /** Contains [delay] and [deviation] in seconds. */
    data class DelayWithDeviation(
            /** In seconds. */
            var delay: Long,
            /** In seconds. */
            var deviation: Long
    ) {
        fun toPair(): Pair<Long, Long> = delay to deviation
    }
    
    @Required
    var transitionIdsToDelays = mutableMapOf<String, DelayWithDeviation>()
    
    @Required
    @Serializable(InstantSerializer::class)
    var generationStart: Instant = Instant.now()
    
    @Required
    var isUsingLifecycle = false
    @Required
    var isSeparatingStartAndFinish: Boolean = true
    
    
    @Required
    var minimumIntervalBetweenActions = 10
    @Required
    var maximumIntervalBetweenActions = 20
    
    
    @Required
    var isUsingResources = false
    
    // if enabled, we ignore simplified resources.
    @Required
    var isUsingComplexResourceSettings = true
    @Required
    var isUsingSynchronizationOnResources = true
    
    @Required
    var simplifiedResources = mutableListOf<String>()
    @Required
    var resourceGroups = mutableListOf<JsonResources.Group>()
    
    // will be converted to resourceMapping
    // not only transitions, but also noise events.
    // resources may be the whole groups, roles or just names.
    @Required
    var transitionIdsToResources = mutableMapOf<String, JsonResources.JsonResourceMapping>()
    
    @Required
    var timeDrivenNoise = JsonTimeDrivenNoise()
    
    init {
        check(minimumIntervalBetweenActions >= 0) { "Field minimumIntervalBetweenActions should not be negative." }
        check(maximumIntervalBetweenActions >= 0) { "Field maximumIntervalBetweenActions should not be negative." }
    }
    
    override fun toString() = reflectionToString(this)
}

/** Json representation of resources */
object JsonResources {
    @Serializable
    data class Group(
            var name: String,
            var roles: MutableList<Role> = mutableListOf()
    ) {
        override fun toString() = reflectionToString(this)
    }
    
    @Serializable
    data class Role(
            var name: String,
            var resources: MutableList<Resource> = mutableListOf()
    ) {
        override fun toString() = reflectionToString(this)
    }
    
    @Serializable
    data class Resource(
            var name: String,
            @Required
            var minDelayBetweenActionsMillis: Long = 15 * 60 * 1000L,
            @Required
            var maxDelayBetweenActionsMillis: Long = 20 * 60 * 1000L
    ) {
        init {
            check(minDelayBetweenActionsMillis >= 0) { "Field minDelayBetweenActionsMillis should not be negative." }
            check(maxDelayBetweenActionsMillis >= 0) { "Field maxDelayBetweenActionsMillis should not be negative." }
        }
        
        override fun toString() = reflectionToString(this)
    }
    
    @Serializable
    class JsonResourceMapping(
            var simplifiedResourceNames: MutableList<String> = mutableListOf(),
            var complexResourceNames: MutableList<String> = mutableListOf(),
            var resourceGroups: MutableList<String> = mutableListOf(),
            var resourceRoles: MutableList<String> = mutableListOf()
    ) {
        override fun toString() = reflectionToString(this)
    }
}