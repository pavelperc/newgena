package org.processmining.models.time_driven_behavior

import org.deckfour.xes.model.XTrace
import org.processmining.models.MovementResult
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.models.abstract_net_representation.WeightedPlace
import org.processmining.models.consumeAllTokens
import org.processmining.models.descriptions.GenerationDescriptionWithNoise
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.organizational_extension.Resource
import org.processmining.utils.TimeDrivenLoggingSingleton
import java.util.*
import kotlin.math.max
import kotlin.random.Random
import org.processmining.models.time_driven_behavior.TimeDrivenTransition.DistortionType.*
import kotlin.random.nextLong

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
            val noiseEvents = mutableListOf<NoiseEvent>()
            
            val noiseDescription = generationDescription.noiseDescription
            if (noiseDescription.isUsingInternalTransitions) {
                noiseEvents += noiseDescription.existingNoiseEvents
            }
            if (noiseDescription.isUsingExternalTransitions) {
                noiseEvents += noiseDescription.artificialNoiseEvents
            }
            return noiseEvents
        }
    
    init {
        val timePair = generationDescription.time[node] ?: TimeDrivenGenerationDescription.DEFAULT_TRANSITION_DELAY
        executionTime = timePair.first
        maxTimeDeviation = timePair.second
    }
    
    val logger: TimeDrivenLoggingSingleton
        get() = TimeDrivenLoggingSingleton.timeDrivenInstance
    
    
    override fun move(trace: XTrace): MovementResult<*>? {
        val movementResult = MovementResult<TimeDrivenToken>()
        movementResult.isActualStep = false
        
        // The time of the first token among all input places. or Long.MAX_VALUE. 
        val time = findMinimalTokenTime() //TODO такой способ нахождения времени не оптимален
        
        if (generationDescription.isUsingSynchronizationOnResources
                && !logger.hasNeededResources(node, time)) {
            
            // make sync steps while we don't get free resources!!!!
            takeSynchronizationStep(movementResult)
            return movementResult
        }
        val tokensHaveTheSameTimestamp = checkTimeOfTokens()
        
        if (tokensHaveTheSameTimestamp) {
            if (shouldDistortEvent()) {
                val distortionType = DistortionType.values().random()
                when (distortionType) {
                    NOISE_BEFORE_ACTUAL_EVENT -> {
                        println("Noise before actual event: $node")//TODO delete?
                        registerNoiseTransition(trace, time, movementResult)
                        if (generationDescription.isUsingSynchronizationOnResources) {
                            if (logger.hasNeededResources(node, time)) {
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
                                if (logger.hasNeededResources(node, time)) {
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
                }
            } else {
                if (generationDescription.isUsingSynchronizationOnResources) {
                    if (logger.hasNeededResources(node, time)) {
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
    
    /** All tokens with the minimal time get the time of the next token after the minimal.*/
    fun takeSynchronizationStep(movementResult: MovementResult<*>) {
        movementResult.isActualStep = false
        
        val smallestTimestamp = findMinimalTokenTime()
        var secondSmallestTimestamp = findNextMinimalTimestamp(smallestTimestamp)
        
        if (generationDescription.isUsingSynchronizationOnResources) {
            val minimalResourceTime = logger.getNearestResourceTime(node)
            secondSmallestTimestamp = max(secondSmallestTimestamp, minimalResourceTime)
        }
        
        if (smallestTimestamp == secondSmallestTimestamp) {
            println("Warning: useless synchronization step on transition ${node.label}.")
        }
        
        
        // increase timestamp from smallest to secondSmallest.
        
        for ((place, weight) in weightedInputPlaces) {
            if (place.lowestTimestamp == smallestTimestamp) {
                val token = place.consumeToken()
                val copy = token.copyTokenWithNewTimestamp(secondSmallestTimestamp)
                place.addToken(copy)
            }
        }
    }
    
    
    /** Check if all input places have the same minimal time. */
    private fun checkTimeOfTokens(): Boolean {
        var time: Long = -1
        for ((place, weight) in weightedInputPlaces) {
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
    
    
    /** Looks minimal token time only among weighted input arcs. */
    fun findMinimalTokenTime(): Long {
        // pavel: reset arcs work in any time. We do not consider them.
        return weightedInputPlaces.map { it.place.lowestTimestamp }.min() ?: Long.MAX_VALUE
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
        // pavel: why we do not make movementResult.isActualStep true???? Where do we do this???
        moveTokensFromPrecedingPlaces(trace, movementResult)
    }
    
    private fun moveTokensFromPrecedingPlaces(trace: XTrace, movementResult: MovementResult<TimeDrivenToken>) {
        val timestamp = consumeTokens()
        startTransition(trace, movementResult, timestamp)
    }
    
    //TODO а нельзя ли как-то хитро заюзать его в базовом классе?
    private fun consumeTokens(): Long {
        var timestamp: Long = 0
        
        inputResetArcPlaces.forEach { place ->
            place.consumeAllTokens()
        }
        for ((place, weight) in weightedInputPlaces) {
            repeat(weight) {
                val consumedToken = place.consumeToken()
                timestamp = consumedToken.timestamp
            }
        }
        return timestamp
    }
    
    private fun startTransition(trace: XTrace, movementResult: MovementResult<TimeDrivenToken>, timeStamp: Long) {
        val timeDeviation = Random.nextLong(-maxTimeDeviation..maxTimeDeviation)
        
        val producedToken: TimeDrivenToken
        if (generationDescription.isUsingResources) {
            val usedResource = logger.logStartEventWithResource(trace, node, timeStamp)
            val finishTime = timeStamp + (executionTime + timeDeviation) * 1000
            if (generationDescription.isUsingSynchronizationOnResources) {
                usedResource?.setTime(finishTime)
            }
            producedToken = TimeDrivenToken(this, finishTime, usedResource)
        } else {
            if (generationDescription.isSeparatingStartAndFinish) {
                logger.log(trace, node, timeStamp, false)
            }
            producedToken = TimeDrivenToken(this, timeStamp + (executionTime + timeDeviation) * 1000)
        }
        movementResult.addProducedExtraToken(producedToken)
    }
    
    private fun registerNoiseTransition(
            trace: XTrace,
            timestamp: Long,
            movementResult: MovementResult<TimeDrivenToken>
    ): Pair<Any, Resource?>? {
        val noiseEvents = noiseEventsBasedOnSettings
        val noiseEventList = LinkedList(noiseEvents)
        while (noiseEventList.size > 0) {
            val noiseEvent = noiseEventList.removeAt(Random.nextInt(noiseEventList.size))
            if (generationDescription.isUsingSynchronizationOnResources
                    && !logger.hasNeededResources(noiseEvent.activity, timestamp)) {
                continue
            }
            var usedResource: Resource? = null
            if (generationDescription.isUsingResources) {
                usedResource = logger.logStartEventWithResource(trace, noiseEvent.activity, timestamp)
            } else {
                if (generationDescription.isSeparatingStartAndFinish) {
                    logger.log(trace, noiseEvent, timestamp, false)
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
            return noiseEvent to usedResource
        }
        return null
    }
    
    private fun shouldDistortEvent(): Boolean {
        if (generationDescription.isUsingNoise) {
            val noiseDescription = generationDescription.noiseDescription
            //use noise transitions
            if (noiseDescription.noisedLevel >= Random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1)) {
                if (noiseDescription.isUsingInternalTransitions || noiseDescription.isUsingExternalTransitions) {
                    return true
                }
            }
        }
        return false
    }
    
    
    /** The next minimal time in input tokens after timestamp */
    private fun findNextMinimalTimestamp(timestamp: Long): Long {
        return weightedInputPlaces
                .map { it.place.lowestTimestamp }
                .filter { it > timestamp }
                .min()
                ?: timestamp
    }
    
    // pavel: what is it? how does it work?
    // it is called from token "move" method.
    // and it logs completing the transition, frees the resources and marks the token to remove from extraTokens.
    fun moveInternalToken(trace: XTrace, token: TimeDrivenToken): MovementResult<*> {
        val movementResult = MovementResult<TimeDrivenToken>()
        // какой-то костыль, чтобы удалить потом этот токен из extraTokens.
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
        logger.log(trace, node, maxTimeStamp, true)
        addTokensToOutputPlaces(maxTimeStamp)
    }
    
    private fun completeTransition(trace: XTrace, resource: Resource, maxTimeStamp: Long) {
        logger.logCompleteEventWithResource(trace, node, resource, maxTimeStamp)
        addTokensToOutputPlaces(maxTimeStamp)
    }
    
    private fun addTokensToOutputPlaces(maxTimeStamp: Long) {
        val description = generationDescription
        val possibleTimeVariation = description.maximumIntervalBetweenActions - description.minimumIntervalBetweenActions
        
        for ((place, weight) in outputPlaces) {
            val timeBetweenActions = description.minimumIntervalBetweenActions + Random.nextInt(possibleTimeVariation + 1)
            
            repeat(weight) {
                val token = TimeDrivenToken(place, maxTimeStamp + timeBetweenActions * 1000)
                place.addToken(token)
            }
        }
    }
    
    
    enum class DistortionType {
        NOISE_INSTEAD_OF_ACTUAL_EVENT,
        NOISE_BEFORE_ACTUAL_EVENT,
        NOISE_AFTER_ACTUAL_EVENT;
    }
}
