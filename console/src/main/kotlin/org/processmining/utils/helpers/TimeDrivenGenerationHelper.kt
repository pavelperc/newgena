package org.processmining.utils.helpers

import org.processmining.models.Movable
import org.processmining.models.abstract_net_representation.Place
import org.processmining.models.abstract_net_representation.Token
import org.processmining.models.abstract_net_representation.Transition
import org.processmining.models.graphbased.NodeID
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.simple_behavior.SimpleTransition
import org.processmining.models.time_driven_behavior.TimeDrivenPlace
import org.processmining.models.time_driven_behavior.TimeDrivenToken
import org.processmining.models.time_driven_behavior.TimeDrivenTransition
import java.time.ZoneOffset

import java.util.*
import kotlin.random.Random

/**
 * Created by Ivan Shugurov on 24.10.2014.
 */
class TimeDrivenGenerationHelper(
        initialMarking: Collection<TimeDrivenPlace>,
        finalMarking: Collection<TimeDrivenPlace>,
        allPlaces: Collection<TimeDrivenPlace>,
        allTransitions: Collection<TimeDrivenTransition>,
        description: TimeDrivenGenerationDescription
) : PetriNetGenerationHelper<TimeDrivenPlace, TimeDrivenTransition, TimeDrivenToken>(initialMarking, finalMarking, allTransitions, allPlaces, description) {
    
    private val generationStart = description.generationStart.toEpochMilli()
    
    override val generationDescription: TimeDrivenGenerationDescription
        get() = super.generationDescription as TimeDrivenGenerationDescription
    
    override fun putInitialToken(place: TimeDrivenPlace) {
        val token = TimeDrivenToken(place, generationStart)
        place.addToken(token)
    }
    
    override fun moveToInitialState() {
        super.moveToInitialState()
        
        val description = generationDescription
        if (description.isUsingSynchronizationOnResources) {
            for (resource in description.simplifiedResources) {
                resource.setTime(0)
                resource.isIdle = true
            }
            for (group in description.resourceGroups) {
                for (resource in group.resources) {
                    resource.setTime(0)
                    resource.isIdle = true
                }
            }
        }
    }
    
    override fun tokensOnlyInFinalMarking(): Boolean {
        return if (extraMovables.isEmpty()) {
            super.tokensOnlyInFinalMarking()
        } else {
            false
        }
    }
    
    override fun chooseNextMovable(): Movable? {
        val enabledTransitions = allModelMovables
                .filter { transition -> transition.checkAvailability() }
                .groupByTo(TreeMap()) { transition -> transition.findMinimalTokenTime() }
        
        
        val movable: Movable?
        if (enabledTransitions.isEmpty() && extraMovables.isEmpty()) {
            return null
        } else {
            if (enabledTransitions.isEmpty()) {
                val tokensMap = sortExtraMovables()
                val entryWithSmallestTimestamp = tokensMap.firstEntry()
                val tokens = entryWithSmallestTimestamp.value
                movable = pickRandomMovable(tokens)
            } else {
                if (extraMovables.isEmpty()) {
                    val entry = enabledTransitions.firstEntry()
                    val movables = entry.value
                    movable = pickRandomMovable(movables)
                } else {
                    val extraMovablesMap = sortExtraMovables()
                    
                    val (earliestTransitionTime, tokensWithSmallestTimestamp) = enabledTransitions.firstEntry()!!
                    val (earliestExtraMovableTime, earliestExtraMovables) = extraMovablesMap.firstEntry()!!
                    
                    if (earliestTransitionTime < earliestExtraMovableTime) {
                        val entry = enabledTransitions.firstEntry()
                        val movables = entry.value
                        movable = pickRandomMovable(movables)
                    } else {
                        if (earliestTransitionTime > earliestExtraMovableTime) {
                            movable = pickRandomMovable(earliestExtraMovables)
                        } else {
                            val useTransition = Random.nextBoolean()
                            if (useTransition) {
                                val entry = enabledTransitions.firstEntry()
                                val movables = entry.value
                                movable = pickRandomMovable(movables)
                            } else {
                                movable = pickRandomMovable(tokensWithSmallestTimestamp)
                            }
                        }
                    }
                    
                }
            }
        }
        return movable
    }
    
    private fun sortExtraMovables() = extraMovables.groupByTo(TreeMap()) { token -> token.timestamp }
    
    companion object {
        
        fun createInstance(petrinet: PetrinetGraph, initialMarking: Marking, finalMarking: Marking, description: TimeDrivenGenerationDescription): TimeDrivenGenerationHelper {
            val idsToLoggablePlaces = petrinet.places.map { it.id to TimeDrivenPlace(it, description) }.toMap()
            val allPlaces = idsToLoggablePlaces.values
    
            val initialPlaces = initialMarking.mapNotNull { idsToLoggablePlaces[it.id] }
            val finalPlaces = finalMarking.mapNotNull { idsToLoggablePlaces[it.id] }
    
    
            val allTransitions = petrinet.transitions.map { transition ->
        
                val (outPlaces, inPlaces, inResetArcPlaces, inInhibitorArcPlaces)
                        = arcsToLoggablePlaces(idsToLoggablePlaces, transition, petrinet)
        
        
                TimeDrivenTransition(transition, description, inPlaces, outPlaces, inInhibitorArcPlaces, inResetArcPlaces)
            }
            
            return TimeDrivenGenerationHelper(initialPlaces, finalPlaces, allPlaces, allTransitions, description)
        }
    }
}
