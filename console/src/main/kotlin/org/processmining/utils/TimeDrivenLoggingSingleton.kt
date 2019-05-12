package org.processmining.utils

import org.deckfour.xes.extension.std.XLifecycleExtension
import org.deckfour.xes.extension.std.XOrganizationalExtension
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.model.XAttribute
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace
import org.processmining.models.descriptions.GenerationDescriptionWithNoise
import org.processmining.models.time_driven_behavior.ResourceMapping
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.descriptions.TimeDrivenGenerationDescription

import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import kotlin.random.Random

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
class TimeDrivenLoggingSingleton protected constructor(
        private val description: TimeDrivenGenerationDescription
) {
    companion object {
        private var singleton: TimeDrivenLoggingSingleton? = null
        
        fun init(description: TimeDrivenGenerationDescription) {
            singleton = TimeDrivenLoggingSingleton(description)
        }
        
        val timeDrivenInstance: TimeDrivenLoggingSingleton
            get() = singleton ?: throw IllegalStateException("TimeDrivenLoggingSingleton is not initialized.")
    }
    
    private val timeExtension = XTimeExtension.instance()
    private val organizationalExtension = XOrganizationalExtension.instance()
    private val lifecycleExtension = XLifecycleExtension.instance()
    
    fun logStartEventWithResource(trace: XTrace, modelActivity: Any, timeStamp: Long): Resource {
        val logEvent = LoggingSingleton.createEvent(modelActivity)
        putLifeCycleAttribute(logEvent, addNoiseToLifecycleProperty(false))
        val usedResource = setResource(modelActivity, logEvent, timeStamp)
        setTimestamp(logEvent, timeStamp)
        if (!shouldSkipEvent() && description.isSeparatingStartAndFinish) {
            trace.add(logEvent)
        }
        return usedResource
    }
    
    fun log(trace: XTrace, modelActivity: Any, timeStamp: Long, isCompleted: Boolean) {
        if (shouldSkipEvent()) {
            return
        }
        val logEvent = createEvent(modelActivity, timeStamp)
        putLifeCycleAttribute(logEvent, addNoiseToLifecycleProperty(isCompleted))
        if (description.isUsingResources) {
            setResource(modelActivity, logEvent, timeStamp)
        }
        trace.add(logEvent)
    }
    
    private fun createEvent(modelActivity: Any, timestamp: Long): XEvent {
        val logEvent = LoggingSingleton.createEvent(modelActivity)
        setTimestamp(logEvent, timestamp)
        return logEvent
    }
    
    private fun setTimestamp(logEvent: XEvent, timestamp: Long) {
        var timestamp = timestamp
        if (shouldDistortTimestamp()) {
            timestamp = distortTimestamp(timestamp)
        }
        timestamp = granulateTimestamp(timestamp)
        val timeAttribute = LoggingSingleton.factory
                .createAttributeLiteral("time:timestamp", Instant.ofEpochMilli(timestamp).toString(), timeExtension)
        logEvent.attributes["time:timestamp"] = timeAttribute
    }
    
    private fun addNoiseToLifecycleProperty(original: Boolean): Boolean {
        val noiseDescription = description.noiseDescription
        if (description.isUsingNoise && description.isSeparatingStartAndFinish && noiseDescription.isUsingLifecycleNoise) {
            if (noiseDescription.noisedLevel >= Random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))
            //use noise transitions
            {
                return !original
            }
        }
        return original
    }
    
    private fun setResource(modelActivity: Any, event: XEvent, timestamp: Long): Resource {
        val availableResources = getAllResourcesMappedToActivity(modelActivity)
        val chosenResource = chooseAvailableResource(availableResources, timestamp)
        if (chosenResource != null) {
            setResource(event, chosenResource)
        } else {
            throw IllegalStateException("Resource is null")
        }
        return chosenResource
    }
    
    private fun chooseAvailableResource(availableResources: MutableList<Resource>, timestamp: Long): Resource? {
        var chosenResource: Resource? = null
        if (description.isUsingSynchronizationOnResources) {
            while (!availableResources.isEmpty() && chosenResource == null) {
                val index = Random.nextInt(availableResources.size)
                val pickedResource = availableResources.removeAt(index)
                if (pickedResource.isIdle && pickedResource.willBeFreed <= timestamp) {
                    chosenResource = pickedResource
                    chosenResource.isIdle = false
                }
            }
        } else {
            val index = Random.nextInt(availableResources.size)
            chosenResource = availableResources[index]
        }
        return chosenResource
    }
    
    fun areResourcesAvailable(modelActivity: Any, timestamp: Long): Boolean {
        if (timestamp < 0) {
            throw IllegalArgumentException("Time cannot be negative")
        }
        val allResourcesMappedToActivity = getAllResourcesMappedToActivity(modelActivity)
        for (resource in allResourcesMappedToActivity) {
            if (resource.isIdle && resource.willBeFreed <= timestamp) {
                return true
            }
        }
        return false
    }
    
    private fun getAllResourcesMappedToActivity(modelActivity: Any): MutableList<Resource> {
        val generalMapping = description.resourceMapping
        val mapping = generalMapping[modelActivity]
                ?: throw IllegalStateException("Can not find resource mapping for activity $modelActivity.")
        
        val availableResources = ArrayList(mapping.selectedResources)
        if (!description.isUsingComplexResourceSettings) {
            availableResources.addAll(mapping.selectedSimplifiedResources)
        }
        return availableResources
    }
    
    private fun shouldDistortTimestamp(): Boolean {
        val noiseDescription = description.noiseDescription
        if (description.isUsingNoise && noiseDescription.isUsingTimestampNoise) {
            if (noiseDescription.noisedLevel >= Random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))
            //use noise transitions
            {
                return true
            }
        }
        return false
    }
    
    private fun distortTimestamp(originalTimestamp: Long): Long {
        val noiseDescription = description.noiseDescription
        
        var deviation = Random.nextInt(noiseDescription.maxTimestampDeviation + 1) * 1000
        
        if (Random.nextBoolean()) {
            deviation = -deviation
        }
        
        var resultedTimestamp = originalTimestamp + deviation
        
        val generationStartTime = description.generationStart
        
        if (resultedTimestamp < generationStartTime.toEpochMilli()) {
            resultedTimestamp = generationStartTime.toEpochMilli()
        }
        return resultedTimestamp
    }
    
    private fun setResource(logEvent: XEvent, resource: Resource) {
        if (description.isUsingComplexResourceSettings) {
            val groupAttribute = LoggingSingleton.factory.createAttributeLiteral("org:group", resource.group!!.toString(),
                    organizationalExtension)
            val roleAttribute = LoggingSingleton.factory.createAttributeLiteral("org:role", resource.role!!.toString(),
                    organizationalExtension)
            val resourceExtension = LoggingSingleton.factory.createAttributeLiteral("org:resource", resource.toString(),
                    organizationalExtension)
            
            logEvent.attributes["org:group"] = groupAttribute
            logEvent.attributes["org:role"] = roleAttribute
            logEvent.attributes["org:resource"] = resourceExtension
        } else {
            val resourceExtension = LoggingSingleton.factory.createAttributeLiteral("org:resource", resource.toString(),
                    organizationalExtension)
            logEvent.attributes["org:resource"] = resourceExtension
        }
    }
    
    private fun granulateTimestamp(timestamp: Long): Long {
        var timestamp = timestamp
        val noiseDescription = description.noiseDescription
        
        if (description.isUsingNoise && noiseDescription.isUsingTimeGranularity) {
            val precision = noiseDescription.granularityType.precision
            val modulo = timestamp % precision
            
            if (modulo * 2 >= precision) {
                timestamp += precision - modulo
            } else {
                timestamp -= modulo
            }
        }
        return timestamp
    }
    
    fun getNearestResourceTime(modelActivity: Any): Long {
        val resources = getAllResourcesMappedToActivity(modelActivity)
        if (resources.isEmpty()) {
            throw IllegalStateException("Can not find resources for activity $modelActivity.")
        }
        
        var leastResourceTime = resources[0].willBeFreed
        for (resource in resources) {
            if (resource.willBeFreed < leastResourceTime) {
                leastResourceTime = resource.willBeFreed
            }
        }
        return leastResourceTime
    }
    
    fun logCompleteEventWithResource(trace: XTrace, modelActivity: Any, resource: Resource, timeStamp: Long) {
        resource.isIdle = true
        if (shouldSkipEvent()) {
            return
        }
        val logEvent = createEvent(modelActivity, timeStamp)
        putLifeCycleAttribute(logEvent, true)
        setResource(logEvent, resource)
        trace.add(logEvent)
    }
    
    private fun putLifeCycleAttribute(logEvent: XEvent, isComplete: Boolean) {
        if (isComplete) {
            putLifeCycleAttribute(logEvent, "complete")
        } else {
            putLifeCycleAttribute(logEvent, "start")
        }
    }
    
    private fun putLifeCycleAttribute(logEvent: XEvent, transition: String) {
        val attribute = LoggingSingleton.factory.createAttributeLiteral("lifecycle:transition", transition, lifecycleExtension)
        logEvent.attributes["lifecycle:transition"] = attribute
    }
    
    private fun shouldSkipEvent(): Boolean {
        val noiseDescription = description.noiseDescription
        if (description.isUsingNoise && noiseDescription.isSkippingTransitions) {
            if (noiseDescription.noisedLevel >= Random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))
            //use noise transitions
            {
                return true
            }
        }
        return false
    }
}
