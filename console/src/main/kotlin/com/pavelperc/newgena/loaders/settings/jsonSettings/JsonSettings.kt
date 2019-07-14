package com.pavelperc.newgena.loaders.settings.jsonSettings

import com.pavelperc.newgena.loaders.settings.InstantSerializer
import com.pavelperc.newgena.loaders.settings.documentation.Doc
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import org.processmining.models.GenerationDescription
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.NoiseEvent
import java.time.Instant
import kotlin.reflect.full.declaredMemberProperties

@Serializable
data class SettingsInfo(
        @Required
        @Doc("For petrinet settings the type is \"petrinet\".")
        val type: String,
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
        const val LAST_SETTINGS_VERSION = "0.5"
    }
    
    @Required
    val settingsInfo = SettingsInfo("petrinet", LAST_SETTINGS_VERSION)
    
    @Required
    var petrinetSetup = JsonPetrinetSetup()
    @Doc("The folder for log files output. " +
            "Relative path will be computed from the tool working directory.")
    @Required
    var outputFolder = "xes-out"
    
    @Required
    @Doc("Number of logs.")
    var numberOfLogs = 5
    @Required
    @Doc("Number of traces in a log.")
    var numberOfTraces = 10
    @Required
    @Doc("Maximum number of steps in a trace. " +
            "After this limit the trace becomes unfinished.")
    var maxNumberOfSteps = 100
    
    @Required
    var isRemovingEmptyTraces = true
    @Required
    @Doc("Do we remove traces, who didn't reach the final marking.")
    var isRemovingUnfinishedTraces = true
    
    @Required
    @Doc("Do we use noise generation.")
    var isUsingNoise = false
    @Required
    var noiseDescription: JsonNoise = JsonNoise()
    
    @Required
    @Doc("Do we use generation with transition priorities. " +
            "(Mutually exclusive with isUsingNoise and isUsingTime!!)")
    var isUsingStaticPriorities: Boolean = false
    @Required
    var staticPriorities: JsonStaticPriorities = JsonStaticPriorities()
    
    @Required
    @Doc("Do we use generation with timestamps. Exclusive with static priorities.")
    var isUsingTime = false
    @Required
    var timeDescription: JsonTimeDescription = JsonTimeDescription()
    
    
    init {
//        check(numberOfLogs >= 0) { "Field numberOfLogs should not be negative." }
//        check(numberOfTraces >= 0) { "Field numberOfTraces should not be negative." }
//        check(maxNumberOfSteps >= 0) { "Field maxNumberOfSteps should not be negative." }
        
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
    @Doc("Ids of initial places in marking and amounts of tokens.")
    var initialPlaceIds = mutableMapOf<String, Int>()
    @Required
    @Doc("Ids of final places in marking and amounts of tokens.")
    var finalPlaceIds = mutableMapOf<String, Int>()
    
    @Required
    @Doc("Do we use initial marking from pnml file.")
    var isUsingInitialMarkingFromPnml = false
    
    override fun toString() = reflectionToString(this)
}

@Serializable
class JsonPetrinetSetup {
    
    @Required
    @Doc("The path to the Petri Net file. " +
            "It is relative to the tool working directory. " +
            "(Or it is a full path).")
    var petrinetFile = "petrinet.pnml"
    @Required
    var marking = JsonMarking()
    override fun toString() = reflectionToString(this)
}

@Serializable
class JsonNoise {
    @Required
    @Doc("Noise level: from 1 to 100.")
    var noiseLevel = 5
    
    @Required
    @Doc("Do we allow skipping transitions during writing to the log.")
    var isSkippingTransitions = true
    
    @Required
    @Doc("Do we add artificial events to the log.")
    var isUsingExternalTransitions = true
    @Required
    @Doc("Do we add existing transitions to the log as a noise.")
    var isUsingInternalTransitions = true
    
    @Required
    @Doc("Existing transitions for noise.")
    var internalTransitionIds = mutableListOf<String>()
    @Required
    @Doc("Artificial noise events. Params:\n" +
            "activity: name of the event\n" +
            "executionTimeSeconds: execution time (is used only in time driven generation)\n" +
            "maxTimeDeviationSeconds: deviation from the execution time.")
    var artificialNoiseEvents = mutableListOf<NoiseEvent>()
    
    override fun toString() = reflectionToString(this)
    
    init {
//        check(noiseLevel in 1..100) { "Field noiseLevel should be in range 1..100 ." }
    }
}

@Serializable
class JsonStaticPriorities {
    /** Ids with larger numbers go first. Default priority is 1.*/
    @Required
    @Doc("Priority dictionary. Transitions with higher priority fire earlier.\n" +
            "Default priority is 1. All priorities should be positive.")
    var transitionIdsToPriorities = mutableMapOf<String, Int>()
    
    override fun toString() = reflectionToString(this)
}

@Serializable
class JsonTimeDrivenNoise {
    
    @Required
    var isUsingTimestampNoise = true
    @Required
    var isUsingLifecycleNoise = true
    @Required
    @Doc("Should we round timestamps with the specified `granularityType`.")
    var isUsingTimeGranularity = true
    @Required
    var maxTimestampDeviationSeconds = 0
    @Required
    @Doc("Setup the precision of timestamps.\n" +
            "The timestamps will be rounded with the specified granularity.")
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
    @Doc("Transition delays in seconds with deviation.\n" +
            "By default for transition delay is 0 with 0 deviation.")
    var transitionIdsToDelays = mutableMapOf<String, DelayWithDeviation>()
    
    @Required
    @Serializable(InstantSerializer::class)
    @Doc("Generation start in ISO-8601 format.\n" +
            "Time zone in logs will be always UTC+0.\n" +
            "Example: \"2019-04-22T01:17:48.509Z\"")
    var generationStart: Instant = Instant.now()
    
    @Required
    @Doc("Do we use lifecycle extension. " +
            "This settings just adds a parameter in log " +
            "to mark event as `start` or `complete`.\n" +
            "To enable transition `complete` event, " +
            "use isSeparatingStartAndFinish option.")
    var isUsingLifecycle = false
    @Required
    @Doc("Enables logging transition `complete` event.\n" +
            "See also isUsingLifecycle option.")
    var isSeparatingStartAndFinish: Boolean = true
    
    
    @Required
    @Doc("A minimum interval between transition firings, in seconds.")
    var minimumIntervalBetweenActions = 10
    @Required
    @Doc("A maximum interval between transition firings, in seconds.")
    var maximumIntervalBetweenActions = 20
    
    
    @Required
    var isUsingResources = false
    
    // if enabled, we ignore simplified resources.
    @Required
    @Doc("Do we use resources with groups and roles.\n" +
            "For now, `true` just disables simplifiedResources.")
    var isUsingComplexResourceSettings = true
    @Required
    
    @Doc("Enables resource synchronization: " +
            "only one transition uses the resource at the same time.")
    var isUsingSynchronizationOnResources = true
    
    @Required
    @Doc("All simplified resources. They have only a name.")
    var simplifiedResources = mutableListOf<String>()
    @Required
    @Doc("Complex resources with groups and roles.")
    var resourceGroups = mutableListOf<JsonResources.Group>()
    
    // will be converted to resourceMapping
    // not only transitions, but also noise events.
    // resources may be the whole groups, roles or just names.
    @Required
    @Doc("Matching transition ids to resources\n" +
            "You can assign the whole role or group or just a resource.\n" +
            "Some transitions may not have any resources, in that case they can be skipped.\n" +
            "Also you can assign resources for noise events, by their name.\n" +
            "If the noise setting is disabled, they are just ignored.")
    var transitionIdsToResources = mutableMapOf<String, JsonResources.JsonResourceMapping>()
    
    @Required
    @Doc("Timestamp noise.")
    var timeDrivenNoise = JsonTimeDrivenNoise()
    
    init {
//        check(minimumIntervalBetweenActions >= 0) { "Field minimumIntervalBetweenActions should not be negative." }
//        check(maximumIntervalBetweenActions >= 0) { "Field maximumIntervalBetweenActions should not be negative." }
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
//            check(minDelayBetweenActionsMillis >= 0) { "Field minDelayBetweenActionsMillis should not be negative." }
//            check(maxDelayBetweenActionsMillis >= 0) { "Field maxDelayBetweenActionsMillis should not be negative." }
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
        fun isEmpty() = simplifiedResourceNames.size +
                complexResourceNames.size +
                resourceGroups.size +
                resourceRoles.size == 0
        
        fun isNotEmpty() = !isEmpty()
        
        override fun toString() = reflectionToString(this)
    }
}