package com.pavelperc.newgena.loaders.settings

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeInt
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeLong
import com.pavelperc.newgena.utils.propertyinitializers.RangeInt
import org.processmining.models.GenerationDescription
import org.processmining.models.time_driven_behavior.GranularityTypes
import org.processmining.models.time_driven_behavior.NoiseEvent
import java.time.Instant
import kotlin.reflect.full.declaredMemberProperties

/** Json representation of all generation settings.
 * Represents all adjustable(!) parameters.
 * Should be json serializable, all properties are mutable.
 * This class is not used during generation, but can be converted in [GenerationDescription] class via [JsonSettingsBuilder].
 */
@JsonPropertyOrder(value = ["petrinetSetup", "outputFolder"], alphabetic = true)
class JsonSettings() {
    
    // JsonCreator constructors are made for Jackson to throw exceptions if one of json fields is missing.
    // But we want to manually create default settings in case of new configuration or replacing nullable fields. 
    // Also kotlin extension for jackson will pass non-existing nullable fields(by setting null)
    // and fields with default parameters.
    // So we don't make primary constructor.
    // Also in primary constructor there will be problems with delegated properties. Which are luckily work well with jackson.
    @JsonCreator
    constructor(
            outputFolder: String,
            petrinetSetup: JsonPetrinetSetup,
            numberOfLogs: Int,
            numberOfTraces: Int,
            maxNumberOfSteps: Int,
            isRemovingEmptyTraces: Boolean,
            isRemovingUnfinishedTraces: Boolean,
            isUsingNoise: Boolean,
            noiseDescription: JsonNoise,
            isUsingStaticPriorities: Boolean,
            staticPriorities: JsonStaticPriorities,
            isUsingTime: Boolean,
            timeDescription: JsonTimeDescription
    ) : this() {
        this.outputFolder = outputFolder
        this.petrinetSetup = petrinetSetup
        this.numberOfLogs = numberOfLogs
        this.numberOfTraces = numberOfTraces
        this.maxNumberOfSteps = maxNumberOfSteps
        this.isRemovingEmptyTraces = isRemovingEmptyTraces
        this.isRemovingUnfinishedTraces = isRemovingUnfinishedTraces
        this.isUsingNoise = isUsingNoise
        this.noiseDescription = noiseDescription
        this.isUsingStaticPriorities = isUsingStaticPriorities
        this.staticPriorities = staticPriorities
        this.isUsingTime = isUsingTime
        this.timeDescription = timeDescription
    }
    
    // ----------------------------- VARIABLES: ---------------------------
    var outputFolder = "xes-out"
    var petrinetSetup = JsonPetrinetSetup()
    
    var numberOfLogs by NonNegativeInt(5)
    var numberOfTraces by NonNegativeInt(10)
    var maxNumberOfSteps by NonNegativeInt(100)
    
    var isRemovingEmptyTraces = true
    var isRemovingUnfinishedTraces = true
    
    var isUsingNoise = false
    var noiseDescription: JsonNoise = JsonNoise()
    
    var isUsingStaticPriorities: Boolean = false
    var staticPriorities: JsonStaticPriorities = JsonStaticPriorities()
    
    var isUsingTime = false
    var timeDescription: JsonTimeDescription = JsonTimeDescription()
    
    companion object {}
    
    override fun toString() = reflectionToString(this)
}

fun reflectionToString(any: Any) =
        any::class.declaredMemberProperties
                .joinToString(",\n", "${any::class.simpleName}(\n", "\n)") { prop ->
                    "${prop.name}: ${prop.call(any)}".prependIndent()
                }


class JsonMarking() {
    @JsonCreator
    constructor(initialPlaceIds: MutableMap<String, Int>, finalPlaceIds: MutableMap<String, Int>, isUsingInitialMarkingFromPnml: Boolean) : this() {
        this.initialPlaceIds = initialPlaceIds
        this.finalPlaceIds = finalPlaceIds
        this.isUsingInitialMarkingFromPnml = isUsingInitialMarkingFromPnml
    }
    
    var initialPlaceIds = mutableMapOf<String, Int>()
    var finalPlaceIds = mutableMapOf<String, Int>()
    
    var isUsingInitialMarkingFromPnml = false
    
    override fun toString() = reflectionToString(this)
}

@JsonPropertyOrder(value = ["petrinetFile"], alphabetic = true)
class JsonPetrinetSetup() {
    
    @JsonCreator
    constructor(
            petrinetFile: String,
            marking: JsonMarking,
            inhibitorArcIds: MutableList<String> = mutableListOf(),
            resetArcIds: MutableList<String> = mutableListOf()
    ) : this() {
        this.marking = marking
        this.inhibitorArcIds = inhibitorArcIds
        this.resetArcIds = resetArcIds
        this.petrinetFile = petrinetFile
    }
    
    var petrinetFile = "petrinet.pnml"
    var marking = JsonMarking()
    var inhibitorArcIds: MutableList<String> = mutableListOf()
    var resetArcIds: MutableList<String> = mutableListOf()
    
    override fun toString() = reflectionToString(this)
}


class JsonNoise() {
    @JsonCreator
    constructor(
            noiseLevel: Int,
            isSkippingTransitions: Boolean,
            isUsingExternalTransitions: Boolean,
            isUsingInternalTransitions: Boolean,
            internalTransitionIds: MutableList<String>,
            artificialNoiseEvents: MutableList<NoiseEvent>
    ) : this() {
        this.noiseLevel = noiseLevel
        this.isSkippingTransitions = isSkippingTransitions
        this.isUsingExternalTransitions = isUsingExternalTransitions
        this.isUsingInternalTransitions = isUsingInternalTransitions
        this.internalTransitionIds = internalTransitionIds
        this.artificialNoiseEvents = artificialNoiseEvents
    }
    
    var noiseLevel: Int by RangeInt(5, 1..100)
    
    var isSkippingTransitions = true
    
    var isUsingExternalTransitions = true
    var isUsingInternalTransitions = true
    
    var internalTransitionIds = mutableListOf<String>()
    var artificialNoiseEvents = mutableListOf(NoiseEvent("NoiseEvent"))
    
    override fun toString() = reflectionToString(this)
}

class JsonStaticPriorities() {
    @JsonCreator
    constructor(maxPriority: Int, transitionIdsToPriorities: MutableMap<String, Int>) : this() {
        this.maxPriority = maxPriority
        this.transitionIdsToPriorities = transitionIdsToPriorities
    }
    
    var maxPriority: Int = 100 // >= 1
    /** Ids with larger numbers go first. Default priority is 1.*/
    var transitionIdsToPriorities = mutableMapOf<String, Int>()
    
    override fun toString() = reflectionToString(this)
}

class JsonTimeDrivenNoise() {
    @JsonCreator
    constructor(
            isUsingTimestampNoise: Boolean,
            isUsingLifecycleNoise: Boolean,
            isUsingTimeGranularity: Boolean,
            maxTimestampDeviationSeconds: Int,
            granularityType: GranularityTypes
    ) : this() {
        this.isUsingTimestampNoise = isUsingTimestampNoise
        this.isUsingLifecycleNoise = isUsingLifecycleNoise
        this.isUsingTimeGranularity = isUsingTimeGranularity
        this.maxTimestampDeviationSeconds = maxTimestampDeviationSeconds
        this.granularityType = granularityType
    }
    
    
    var isUsingTimestampNoise: Boolean = true
    var isUsingLifecycleNoise: Boolean = true
    var isUsingTimeGranularity: Boolean = true
    var maxTimestampDeviationSeconds: Int = 0
    var granularityType: GranularityTypes = GranularityTypes.MINUTES_5
    
    override fun toString() = reflectionToString(this)
}

/** Json representation of TimeDescription */
class JsonTimeDescription {
    data class DelayWithDeviation @JsonCreator constructor(
            var delay: Long,
            var deviation: Long
    ) {
        fun toPair(): Pair<Long, Long> = delay to deviation
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
    
    var timeDrivenNoise: JsonTimeDrivenNoise = JsonTimeDrivenNoise()
    
    override fun toString() = reflectionToString(this)
}

/** Json representation of resources */
object JsonResources {
    class Role() {
        @JsonCreator
        constructor(name: String, resources: MutableList<Resource>) : this() {
            this.name = name
            this.resources = resources
        }
        
        var name: String = "role"
        var resources = mutableListOf<Resource>(Resource())
    
        override fun toString() = reflectionToString(this)
    }
    
    class Resource() {
        @JsonCreator
        constructor(name: String, willBeFreed: Long, minDelayBetweenActionsMillis: Long, maxDelayBetweenActionsMillis: Long) : this() {
            this.name = name
            this.willBeFreed = willBeFreed
            this.minDelayBetweenActionsMillis = minDelayBetweenActionsMillis
            this.maxDelayBetweenActionsMillis = maxDelayBetweenActionsMillis
        }
        
        var name: String = "resource"
        var willBeFreed by NonNegativeLong(0L)
        var minDelayBetweenActionsMillis by NonNegativeLong(15 * 60 * 1000)
        var maxDelayBetweenActionsMillis by NonNegativeLong(20 * 60 * 1000)
    
        override fun toString() = reflectionToString(this)
    }
    
    class Group() {
        @JsonCreator
        constructor(name: String, roles: MutableList<Role>) : this() {
            this.name = name
            this.roles = roles
        }
        
        var name: String = "group"
        var roles = mutableListOf<Role>(Role())
    
        override fun toString() = reflectionToString(this)
    }
    
    class ResourceMapping() {
        data class FullResourceName(
                var groupName: String,
                var roleName: String,
                var resourceName: String
        )
        
        @JsonCreator
        constructor(fullResourceNames: MutableList<FullResourceName>, simplifiedResourceNames: MutableList<String>) : this() {
            this.fullResourceNames = fullResourceNames
            this.simplifiedResourceNames = simplifiedResourceNames
        }
        
        var fullResourceNames = mutableListOf<FullResourceName>()
        var simplifiedResourceNames = mutableListOf<String>()
    
        override fun toString() = reflectionToString(this)
    }
}