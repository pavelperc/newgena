package org.processmining.utils.helpers

import com.pavelperc.newgena.utils.common.firstValue
import com.pavelperc.newgena.utils.common.randomOrNull
import org.processmining.models.Movable
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.time_driven_behavior.TimeDrivenPlace
import org.processmining.models.time_driven_behavior.TimeDrivenToken
import org.processmining.models.time_driven_behavior.TimeDrivenTransition

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
        // transitions, grouped and sorted by earliest token
        val enabledTransitions = allModelMovables
                .filter { transition -> transition.checkAvailability() }
                .groupByTo(TreeMap()) { transition -> transition.findMinimalTokenTime() }
        
        // extra tokens, grouped and sorted by earliest time
        val tokensMap = extraMovables.groupByTo(TreeMap()) { token -> token.timestamp }
        
        
        return when {
            enabledTransitions.isEmpty() && tokensMap.isEmpty() ->
                null
            // only tokens left
            enabledTransitions.isEmpty() -> {
                // random token with the smallest timestamp
                val earliestTokens = tokensMap.firstValue()!!
                earliestTokens.randomOrNull()
            }
            // only transitions left
            tokensMap.isEmpty() -> {
                // random transition with the earliest token
                val earliestTransitions = enabledTransitions.firstValue()!!
                earliestTransitions.randomOrNull()
            }
            // we have tokens and transitions.
            else -> {
                val (earliestTransitionTime, earliestTransitions) = enabledTransitions.firstEntry()!!
                val (earliestTokenTime, earliestTokens) = tokensMap.firstEntry()!!
                
                when {
                    earliestTransitionTime < earliestTokenTime ->
                        earliestTransitions.randomOrNull()
                    earliestTransitionTime > earliestTokenTime ->
                        earliestTokens.randomOrNull()
                    // first token or transition are equal
                    else -> {
                        // TODO: is a transition enabled, when it waits a resource?
                        // if so, then why we don't move all tokens firstly to free a resources?
//                        val useTransition = Random.nextBoolean()
                        val useTransition = false
                        if (useTransition) {
                            earliestTransitions.randomOrNull() as Movable?
                        } else {
                            earliestTokens.randomOrNull() as Movable?
                        }
                    }
                }
                
            }
        }
    }
    
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
