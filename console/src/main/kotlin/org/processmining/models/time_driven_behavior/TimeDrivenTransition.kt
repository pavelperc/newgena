package org.processmining.models.time_driven_behavior

import org.deckfour.xes.model.XTrace
import org.processmining.framework.util.Pair
import org.processmining.models.MovementResult
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.models.abstract_net_representation.WeightedPlace
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.organizational_extension.Resource
import org.processmining.utils.TimeDrivenLoggingSingleton

import java.util.ArrayList
import java.util.LinkedList
import kotlin.random.Random

private typealias WPlace = WeightedPlace<TimeDrivenToken, TimeDrivenPlace>

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
class TimeDrivenTransition(
        node: org.processmining.models.graphbased.directed.petrinet.elements.Transition,
        generationDescription: TimeDrivenGenerationDescription,
        inputPlaces: List<WPlace>,
        outputPlaces: List<WPlace>,
        inputInhibitorArcPlaces: List<TimeDrivenPlace> = listOf(),
        inputResetArcPlaces: List<TimeDrivenPlace> = listOf()
) : Transition<TimeDrivenToken, TimeDrivenPlace>(node, generationDescription, inputPlaces, outputPlaces, inputInhibitorArcPlaces, inputResetArcPlaces),
        Comparable<TimeDrivenTransition> {
    
    override val generationDescription: TimeDrivenGenerationDescription
        get() = super.generationDescription as TimeDrivenGenerationDescription
    
    private val executionTime: Long
    private val maxTimeDeviation: Long
    
    private val noiseEventsBasedOnSettings: List<NoiseEvent>
        get() {
            val noiseEvents = ArrayList<NoiseEvent>()
            val noiseDescription = generationDescription.noiseDescription
            if (noiseDescription.isUsingInternalTransitions) {
                noiseEvents.addAll(noiseDescription.existingNoiseEvents)
            }
            if (noiseDescription.isUsingExternalTransitions) {
                noiseEvents.addAll(noiseDescription.artificialNoiseEvents)
            }
            return noiseEvents
        }
    
    init {
        val timePair = generationDescription.time.getValue(node)
        executionTime = timePair.first
        maxTimeDeviation = timePair.second
    }
    
    override fun move(trace: XTrace): MovementResult<*>? {
        val movementResult = MovementResult<TimeDrivenToken>()
        movementResult.isActualStep = false
        val time = findMinimalTokenTime() //TODO такой способ нахождения времени не оптимален
        if (generationDescription.isUsingSynchronizationOnResources && !TimeDrivenLoggingSingleton.timeDrivenInstance.areResourcesAvailable(node, time)) {
            takeSynchronizationStep(movementResult)
            return movementResult
        }
        val tokensHaveTheSameTimestamp = checkTimeOfTokens()
        
        if (tokensHaveTheSameTimestamp) {
            if (shouldDistortEvent()) {
                val distortionType = Random.nextInt(3)
                
                when (distortionType) {
                    NOISE_BEFORE_ACTUAL_EVENT -> {
                        println("Noise before actual event: $node")//TODO delete?
                        registerNoiseTransition(trace, time, movementResult)
                        if (generationDescription.isUsingSynchronizationOnResources) {
                            if (TimeDrivenLoggingSingleton.timeDrivenInstance.areResourcesAvailable(node, time)) {
                                actuallyMove(trace, movementResult)
                            } else {
                                takeSynchronizationStep(movementResult)
                            }
                        } else {
                            actuallyMove(trace, movementResult)
                        }
                    }
                    NOISE_AFTER_ACTUAL_EVENT -> {
                        println("Noise after actual event: $node") //TODO delete?
                        actuallyMove(trace, movementResult)
                        registerNoiseTransition(trace, time, movementResult)
                    }
                    NOISE_INSTEAD_OF_ACTUAL_EVENT -> {
                        println("Noise instead of actual event: $node") //TODO delete?
                        val extraResult = MovementResult<TimeDrivenToken>()
                        extraResult.isActualStep = false
                        val registeredPair = registerNoiseTransition(trace, time, extraResult)
                        if (registeredPair == null) {
                            if (generationDescription.isUsingSynchronizationOnResources) {
                                if (TimeDrivenLoggingSingleton.timeDrivenInstance.areResourcesAvailable(node, time)) {
                                    actuallyMove(trace, movementResult)
                                } else {
                                    takeSynchronizationStep(movementResult)
                                }
                            } else {
                                actuallyMove(trace, movementResult)
                            }
                        } else {
                            consumeTokens()
                            val createdTokens = extraResult.producedExtraMovables
                            val noiseToken = createdTokens[0]
                            val noiseTimestamp = noiseToken.timestamp
                            val replacementToken = ReplacementToken(generationDescription, this, registeredPair.first, registeredPair.second, noiseTimestamp)
                            movementResult.addProducedExtraToken(replacementToken)
                        }
                    }
                    else -> throw IllegalArgumentException("Incorrect type of noise $distortionType")
                }
            } else {
                if (generationDescription.isUsingSynchronizationOnResources) {
                    if (TimeDrivenLoggingSingleton.timeDrivenInstance.areResourcesAvailable(node, time)) {
                        actuallyMove(trace, movementResult)
                    } else {
                        takeSynchronizationStep(movementResult)
                    }
                } else {
                    actuallyMove(trace, movementResult)
                }
            }
        } else {
            takeSynchronizationStep(movementResult)
        }
        return movementResult
    }
    
    private fun takeSynchronizationStep(movementResult: MovementResult<*>) {
        movementResult.isActualStep = false
        val smallestTimestamp = findMinimalTokenTime()
        var secondSmallestTimestamp = findNextMinimalTimestamp(smallestTimestamp)
        if (generationDescription.isUsingSynchronizationOnResources) {
            val minimalResourceTime = TimeDrivenLoggingSingleton.timeDrivenInstance.getNearestResourceTime(node)
            if (secondSmallestTimestamp < minimalResourceTime) {
                secondSmallestTimestamp = minimalResourceTime
            }
        }
        for ((place, weight) in inputPlaces) {
            if (place.lowestTimestamp == smallestTimestamp) {
                val token = place.consumeToken()
                val copy = token.copyTokenWithNewTimestamp(secondSmallestTimestamp)
                place.addToken(copy)
            }
        }
    }
    
    
    private fun checkTimeOfTokens(): Boolean {
        var time: Long = -1
        for ((place, weight) in inputPlaces) {
            if (time == -1L) {
                time = place.lowestTimestamp
            } else {
                if (time != place.lowestTimestamp) {
                    return false
                }
            }
        }
        return true
    }
    
    
    fun findMinimalTokenTime(): Long {
        var minimalTimestamp = java.lang.Long.MAX_VALUE
        for ((place, weight) in inputPlaces) {
            if (minimalTimestamp > place.lowestTimestamp) {
                minimalTimestamp = place.lowestTimestamp
            }
        }
        return minimalTimestamp
    }
    
    override fun compareTo(other: TimeDrivenTransition): Int {
        val instanceMinimalTimestamp = findMinimalTokenTime()
        val parameterMinimalTimestamp = other.findMinimalTokenTime()
        return when {
            instanceMinimalTimestamp == parameterMinimalTimestamp -> 0
            instanceMinimalTimestamp < parameterMinimalTimestamp -> -1
            else -> 1
        }
    }
    
    private fun actuallyMove(trace: XTrace, movementResult: MovementResult<TimeDrivenToken>) {
        moveTokensFromPrecedingPlaces(trace, movementResult)
    }
    
    private fun moveTokensFromPrecedingPlaces(trace: XTrace, movementResult: MovementResult<TimeDrivenToken>) {
        val timestamp = consumeTokens()
        startTransition(trace, movementResult, timestamp)
    }
    
    private fun consumeTokens()//TODO а нельзя ли как-то хитро заюзать его в базовом классе?
            : Long {
        var timestamp: Long = 0
        for ((place, weight) in inputPlaces) {
            val consumedToken = place.consumeToken()
            timestamp = consumedToken.timestamp
        }
        return timestamp
    }
    
    private fun startTransition(trace: XTrace, movementResult: MovementResult<TimeDrivenToken>, timeStamp: Long) {
        var timeDeviation = (Random.nextDouble() * (maxTimeDeviation + 1)).toLong()
        if (Random.nextBoolean()) {
            timeDeviation = -timeDeviation
        }
        val producedToken: TimeDrivenToken
        if (generationDescription.isUsingResources) {
            val usedResource = TimeDrivenLoggingSingleton.timeDrivenInstance.logStartEventWithResource(trace, node, timeStamp)
            val finishTime = timeStamp + (executionTime + timeDeviation) * 1000
            if (generationDescription.isUsingSynchronizationOnResources) {
                usedResource.setTime(finishTime)
                setResourceTime(usedResource, finishTime)
            }
            producedToken = TimeDrivenToken(this, finishTime, usedResource)
        } else {
            if (generationDescription.isSeparatingStartAndFinish) {
                TimeDrivenLoggingSingleton.timeDrivenInstance.log(trace, node, timeStamp, false)
            }
            producedToken = TimeDrivenToken(this, timeStamp + (executionTime + timeDeviation) * 1000)
        }
        movementResult.addProducedExtraToken(producedToken)
    }
    
    private fun setResourceTime(resource: Resource, finishTime: Long) {
        var finishTime = finishTime
        if (generationDescription.isUsingSynchronizationOnResources) {
            val minDelay = resource.minDelayBetweenActions
            val maxDelay = resource.maxDelayBetweenActions
            val difference = maxDelay - minDelay
            val actualDelay = Random.nextLong() % (difference + 1)
            finishTime += minDelay + actualDelay
        }
        resource.setTime(finishTime)
    }
    
    private fun registerNoiseTransition(trace: XTrace, timestamp: Long, movementResult: MovementResult<TimeDrivenToken>): Pair<Any, Resource>? {
        val loggingSingleton = TimeDrivenLoggingSingleton.timeDrivenInstance
        val noiseEvents = noiseEventsBasedOnSettings
        val noiseEventList = LinkedList(noiseEvents)
        while (noiseEventList.size > 0) {
            println("Number of noise events: " + noiseEventList.size)
            val noiseEvent = noiseEventList.removeAt(Random.nextInt(noiseEventList.size))
            if (generationDescription.isUsingSynchronizationOnResources && !loggingSingleton.areResourcesAvailable(noiseEvent.activity, timestamp)) {
                continue
            }
            println("Added " + noiseEvent.activity + " as a noise event") //TODO delete?
            var usedResource: Resource? = null
            if (generationDescription.isUsingResources) {
                usedResource = loggingSingleton.logStartEventWithResource(trace, noiseEvent.activity, timestamp)
            } else {
                if (generationDescription.isSeparatingStartAndFinish) {
                    loggingSingleton.log(trace, noiseEvent, timestamp, false)
                }
            }
            var timeDeviation = (Random.nextDouble() * (noiseEvent.maxTimeDeviationSeconds + 1)).toLong()
            if (Random.nextBoolean()) {
                timeDeviation = -timeDeviation
            }
            val totalExecutionTime = (noiseEvent.executionTimeSeconds + timeDeviation) * 1000
            val finishTime = timestamp + totalExecutionTime
            if (usedResource != null && generationDescription.isUsingSynchronizationOnResources) {
                usedResource.setTime(finishTime)
            }
            val noiseToken = NoiseToken(noiseEvent, finishTime, usedResource)
            movementResult.addProducedExtraToken(noiseToken)
            return Pair<Any, Resource>(noiseEvent, usedResource)
        }
        return null
    }
    
    private fun shouldDistortEvent(): Boolean {
        if (generationDescription.isUsingNoise) {
            val noiseDescription = generationDescription.noiseDescription
            if (noiseDescription.noisedLevel >= Random.nextInt(org.processmining.models.descriptions.GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))
            //use noise transitions
            {
                if (noiseDescription.isUsingInternalTransitions || noiseDescription.isUsingExternalTransitions) {
                    return true
                }
            }
        }
        return false
    }
    
    private fun findNextMinimalTimestamp(timestamp: Long): Long {
        var nextMinimalTimestamp = java.lang.Long.MAX_VALUE
        for ((place, weight) in inputPlaces) {
            val currentTimestamp = place.lowestTimestamp
            if (currentTimestamp < nextMinimalTimestamp && currentTimestamp > timestamp) {
                nextMinimalTimestamp = currentTimestamp
            }
        }
        if (nextMinimalTimestamp == java.lang.Long.MAX_VALUE) {
            nextMinimalTimestamp = timestamp
        }
        return nextMinimalTimestamp
    }
    
    fun moveInternalToken(trace: XTrace, token: TimeDrivenToken): MovementResult<*> {
        val movementResult = MovementResult<TimeDrivenToken>()
        movementResult.addConsumedExtraToken(token)
        val maxTimeStamp = token.timestamp
        if (generationDescription.isUsingResources && token.resource != null) {
            completeTransition(trace, token.resource, maxTimeStamp)
        } else {
            completeTransition(trace, maxTimeStamp)
        }
        return movementResult
    }
    
    private fun completeTransition(trace: XTrace, maxTimeStamp: Long) {
        TimeDrivenLoggingSingleton.timeDrivenInstance.log(trace, node, maxTimeStamp, true)
        addTokensToOutputPlaces(maxTimeStamp)
    }
    
    private fun completeTransition(trace: XTrace, resource: Resource, maxTimeStamp: Long) {
        TimeDrivenLoggingSingleton.timeDrivenInstance.logCompleteEventWithResource(trace, node, resource, maxTimeStamp)
        addTokensToOutputPlaces(maxTimeStamp)
    }
    
    private fun addTokensToOutputPlaces(maxTimeStamp: Long) {
        val description = generationDescription
        val possibleTimeVariation = description.maximumIntervalBetweenActions - description.minimumIntervalBetweenActions
        for ((place, weight) in outputPlaces) {
            val timeBetweenActions = description.minimumIntervalBetweenActions + Random.nextInt(possibleTimeVariation + 1)
            val token = TimeDrivenToken(place, maxTimeStamp + timeBetweenActions * 1000)
            place.addToken(token)
        }
    }
    
    companion object {
        private val NOISE_INSTEAD_OF_ACTUAL_EVENT = 2
        private val NOISE_BEFORE_ACTUAL_EVENT = 0
        private val NOISE_AFTER_ACTUAL_EVENT = 1
    }
}
