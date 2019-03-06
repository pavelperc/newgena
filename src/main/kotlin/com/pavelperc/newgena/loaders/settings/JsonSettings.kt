package com.pavelperc.newgena.loaders.settings

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonPropertyOrder
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
@JsonPropertyOrder(value = ["petrinetFile", "marking", "outputFolder"], alphabetic = true)
class JsonSettings() {
    
    // JsonCreator constructors are made for Jackson to throw exceptions if one of json fields is missing.
    // But we want to manually create default settings in case of new configuration or replacing nullable fields. 
    // Also kotlin extension for jackson will pass non-existing nullable fields(by setting null)
    // and fields with default parameters.
    // So we don't make primary constructor.
    // Also in primary constructor there will be problems with delegated properties. Which are luckily work well with jackson.
    @JsonCreator
    constructor(
            petrinetFile: String,
            outputFolder: String,
            marking: JsonMarking,
            numberOfLogs: Int,
            numberOfTraces: Int,
            maxNumberOfSteps: Int,
            isRemovingEmptyTraces: Boolean,
            isRemovingUnfinishedTraces: Boolean,
            isUsingNoise: Boolean,
            noiseDescription: JsonNoise?,
            isUsingStaticPriorities: Boolean,
            staticPriorities: JsonStaticPriorities?,
            isUsingTime: Boolean,
            timeDescription: JsonTimeDescription?
    ) : this() {
        this.petrinetFile = petrinetFile
        this.outputFolder = outputFolder
        this.marking = marking
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
    
    var petrinetFile = "petrinet.pnml"
    var outputFolder = "xes-out"
    
    var marking = JsonMarking()
    var numberOfLogs by NonNegativeInt(5)
    var numberOfTraces by NonNegativeInt(10)
    
    var maxNumberOfSteps by NonNegativeInt(100)
    
    var isRemovingEmptyTraces = true
    
    var isRemovingUnfinishedTraces = true
    
    var isUsingNoise by ExclusiveBoolean(::isUsingStaticPriorities)
    
    var noiseDescription: JsonNoise? = JsonNoise()
    
    var isUsingStaticPriorities: Boolean by ExclusiveBoolean(::isUsingStaticPriorities)
    
    var staticPriorities: JsonStaticPriorities? = JsonStaticPriorities()
    
    var isUsingTime by ExclusiveBoolean(::isUsingStaticPriorities)
    
    var timeDescription: JsonTimeDescription? = JsonTimeDescription()
    
    companion object
}

class JsonMarking() {
    @JsonCreator
    constructor(initialPlaceIds: MutableList<String>, finalPlaceIds: MutableList<String>, isUsingInitialMarkingFromPnml: Boolean) : this() {
        this.initialPlaceIds = initialPlaceIds
        this.finalPlaceIds = finalPlaceIds
        this.isUsingInitialMarkingFromPnml = isUsingInitialMarkingFromPnml
    }
    
    var initialPlaceIds = mutableListOf<String>()
    var finalPlaceIds = mutableListOf<String>()
    
    var isUsingInitialMarkingFromPnml = false
}


class JsonNoise() {
    @JsonCreator
    constructor(
            noiseLevel: Int,
            isSkippingTransitions: Boolean,
            isUsingExternalTransitions: Boolean,
            isUsingInternalTransitions: Boolean,
            internalTransitionIds: MutableSet<String>,
            existingNoiseEvents: MutableList<NoiseEvent>
    ) : this() {
        this.noiseLevel = noiseLevel
        this.isSkippingTransitions = isSkippingTransitions
        this.isUsingExternalTransitions = isUsingExternalTransitions
        this.isUsingInternalTransitions = isUsingInternalTransitions
        this.internalTransitionIds = internalTransitionIds
        this.existingNoiseEvents = existingNoiseEvents
    }
    
    var noiseLevel: Int by RangeInt(5, 1..100)
    
    var isSkippingTransitions = true
    
    var isUsingExternalTransitions = true
    var isUsingInternalTransitions = true
    
    var internalTransitionIds = mutableSetOf<String>()
    var existingNoiseEvents = mutableListOf(NoiseEvent("NoiseEvent"))
}

class JsonStaticPriorities() {
    @JsonCreator
    constructor(maxPriority: Int, transitionIdsToPriorities: MutableMap<String, Int>) : this() {
        this.maxPriority = maxPriority
        this.transitionIdsToPriorities = transitionIdsToPriorities
    }
    
    var maxPriority: Int = 1
    /** Ids with larger numbers go first. Default priority is 1.*/
    var transitionIdsToPriorities = mutableMapOf<String, Int>()
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
    
    var timeDrivenNoise: JsonTimeDrivenNoise? = JsonTimeDrivenNoise()
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
    }
    
    class Group() {
        @JsonCreator
        constructor(name: String, roles: MutableList<Role>) : this() {
            this.name = name
            this.roles = roles
        }
        
        var name: String = "group"
        var roles = mutableListOf<Role>(Role())
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
    }
}