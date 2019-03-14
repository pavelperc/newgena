package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.models.markInhResetArcsByIds
import com.pavelperc.newgena.models.pnmlId
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.*
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.organizational_extension.Role
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.time_driven_behavior.ResourceMapping

class JsonSettingsBuilder(val petrinet: PetrinetGraph, val jsonSettings: JsonSettings) {
    
    private val idsToTransitions = petrinet.transitions.map { it.pnmlId to it!! }.toMap()
    
    private fun String.toTrans() = idsToTransitions.getValue(this)
    
    
    fun buildDescription() = jsonSettings.build()
    
    
    /** Returns pair of initialMarking and finalMarking */
    fun buildMarking(): Pair<Marking, Marking> {
        return jsonSettings.petrinetSetup.marking.run {
            
            val idsToPlaces = petrinet.places.map { it.pnmlId to it!! }.toMap()
            
            val initialMarking = initialPlaceIds.map { idsToPlaces.getValue(it) }
            val finalMarking = finalPlaceIds.map { idsToPlaces.getValue(it) }
            
            Marking(initialMarking) to Marking(finalMarking)
        }
    }
    
    private fun JsonSettings.build(): GenerationDescription {
        val description: GenerationDescription
        // where to put checkers???
        // всё равно состояние настроек не будет до конца устойчивым...
        // может сделать все чекеры на уровне интерфейса и билдера?
        checkExclusive(isUsingTime to "isUsingTime", isUsingStaticPriorities to "isUsingStaticPriorities")
        
        
        if (isUsingStaticPriorities) {
            description = staticPriorities?.run {
                GenerationDescriptionWithStaticPriorities(
                        maxPriority = maxPriority,
                        numberOfLogs = numberOfLogs,
                        numberOfTraces = numberOfTraces,
                        maxNumberOfSteps = maxNumberOfSteps,
                        isRemovingUnfinishedTraces = isRemovingUnfinishedTraces,
                        isRemovingEmptyTraces = isRemovingEmptyTraces,
                        priorities = transitionIdsToPriorities.mapKeys { it.key.toTrans() }
                )
            } ?: throw IllegalStateException("staticPriorities is null, but isUsingStaticPriorities is true.")
        } else {
            
            val noiseDescriptionCreator: NoiseDescriptionCreator
            if (isUsingNoise) {
                noiseDescriptionCreator = noiseDescription?.build()
                        ?: throw IllegalStateException("noiseDescription is null, but isUsingNoise is true.")
            } else
                noiseDescriptionCreator = { NoiseDescription() }
            
            if (isUsingTime) {
                
                description = timeDescription?.build(this)
                        ?: throw IllegalStateException("timeDescription is null, but isUsingTime is true.")
                
            } else {
                description = SimpleGenerationDescription(
                        numberOfLogs = numberOfLogs,
                        numberOfTraces = numberOfTraces,
                        maxNumberOfSteps = maxNumberOfSteps,
                        isUsingNoise = isUsingNoise,
                        isRemovingUnfinishedTraces = isRemovingUnfinishedTraces,
                        isRemovingEmptyTraces = isRemovingEmptyTraces,
                        noiseDescriptionCreator = noiseDescriptionCreator
                )
            }
        }
        return description
    }
    
    private fun JsonTimeDescription.build(jsonSettings: JsonSettings): TimeDrivenGenerationDescription {
        return jsonSettings.run {
            
            var simpleRes = listOf<Resource>() // without groups and roles
            var resGroups = listOf<Group>()
            var resMapping = emptyMap<Any, ResourceMapping>()
            if (isUsingResources) {
                
                simpleRes = simplifiedResources.map { it.buildSimplified() }
                resGroups = resourceGroups.map { it.build() }
                val complexRes = resGroups.flatMap { it.resources }
                
                val simpleResFromNames = simpleRes.map { it.name to it }.toMap()
                val complexResFromFullNames = complexRes.map {
                    JsonResources.ResourceMapping.FullResourceName(it.group?.name
                            ?: "null", it.role?.name
                            ?: "null", it.name) to it
                }.toMap()
                
                resMapping = transitionIdsToResources
                        .mapValues { (transId, mapping) ->
                            if (mapping.fullResourceNames.size + mapping.simplifiedResourceNames.size < 1) {
                                throw IllegalStateException("Error in transitionIdsToResources: Transition $transId should have at least one resource.")
                            }
                            ResourceMapping(
                                    selectedSimplifiedResources = mapping.simplifiedResourceNames.map { simpleResFromNames.getValue(it) },
                                    selectedResources = mapping.fullResourceNames.map { complexResFromFullNames.getValue(it) }
                            )
                        }
                        .mapKeys { (transId, _) -> transId.toTrans() as Any }
                
                if (resMapping.size != petrinet.transitions.size) {
                    throw IllegalStateException("transitionIdsToResources mapping should be specified for each transition.")
                }
                
            }
            
            
            val timeNoiseDescriptionCreator: TimeNoiseDescriptionCreator
            if (isUsingNoise) {
                val commonNoise = noiseDescription
                        ?: throw IllegalStateException("noiseDescription is null, but isUsingNoise is true.")
                val timeNoise = timeDrivenNoise
                        ?: throw IllegalStateException("timeDrivenNoise is null, but isUsingNoise is true.")
                
                timeNoiseDescriptionCreator = timeNoise.build(commonNoise)
            } else {
                timeNoiseDescriptionCreator = { TimeNoiseDescription() }
            }
            
            TimeDrivenGenerationDescription(
                    numberOfLogs = numberOfLogs,
                    numberOfTraces = numberOfLogs,
                    maxNumberOfSteps = maxNumberOfSteps,
                    isUsingNoise = isUsingNoise,
                    isUsingResources = isUsingResources,
                    isRemovingUnfinishedTraces = isRemovingUnfinishedTraces,
                    isRemovingEmptyTraces = isRemovingEmptyTraces,
                    isUsingComplexResourceSettings = isUsingComplexResourceSettings,
                    isUsingSynchronizationOnResources = isUsingSynchronizationOnResources,
                    minimumIntervalBetweenActions = minimumIntervalBetweenActions,
                    maximumIntervalBetweenActions = maximumIntervalBetweenActions,
                    isSeparatingStartAndFinish = isSeparatingStartAndFinish,
                    simplifiedResources = simpleRes,
                    isUsingTime = isUsingTime,
                    time = transitionIdsToDelays.map { (id, delay) -> id.toTrans() to delay.toPair() }.toMap(),
                    isUsingLifecycle = isUsingLifecycle,
                    generationStart = generationStart,
                    resourceMapping = resMapping,
                    resourceGroups = resGroups,
                    noiseDescriptionCreator = timeNoiseDescriptionCreator
            )
        }
    }
    
    private fun JsonNoise.build():
            NoiseDescriptionCreator = {
        NoiseDescription(
                noisedLevel = noiseLevel,
                isUsingExternalTransitions = isUsingExternalTransitions,
                isUsingInternalTransitions = isUsingInternalTransitions,
                isSkippingTransitions = isSkippingTransitions,
                internalTransitions = internalTransitionIds.map { it.toTrans() },
                existingNoiseEvents = existingNoiseEvents
        )
    }
    
    private fun JsonTimeDrivenNoise.build(noiseSettings: JsonNoise):
            TimeNoiseDescriptionCreator {
        return noiseSettings.run {
            {
                TimeNoiseDescription(
                        isUsingTimestampNoise = isUsingTimestampNoise,
                        isUsingLifecycleNoise = isUsingLifecycleNoise,
                        isUsingTimeGranularity = isUsingTimeGranularity,
                        maxTimestampDeviation = maxTimestampDeviationSeconds,
                        granularityType = granularityType,
                        
                        noisedLevel = noiseLevel,
                        isUsingExternalTransitions = isUsingExternalTransitions,
                        isUsingInternalTransitions = isUsingInternalTransitions,
                        isSkippingTransitions = isSkippingTransitions,
                        internalTransitions = internalTransitionIds.map { it.toTrans() },
                        existingNoiseEvents = existingNoiseEvents
                )
            }
        }
    }
    
    
    private fun JsonResources.Resource.buildSimplified(): Resource {
        return Resource(
                name = name,
                willBeFreed = willBeFreed,
                minDelayBetweenActions = minDelayBetweenActionsMillis,
                maxDelayBetweenActions = maxDelayBetweenActionsMillis,
                group = null,
                role = null
        )
    }
    
    private fun JsonResources.Group.build(): Group {
        // we create roles and resources in special lambdas - creators,
        // because all roles need groups, all resources need roles and groups and vice versa
        
        // oldRole, oldRes mean json role and res
        return Group(name) { newGroup ->
            roles.map { oldRole ->
                Role(
                        name = oldRole.name,
                        group = newGroup
                ) { _, newRole ->
                    oldRole.resources.map { oldRes ->
                        Resource(
                                name = oldRes.name,
                                willBeFreed = oldRes.willBeFreed,
                                minDelayBetweenActions = oldRes.minDelayBetweenActionsMillis,
                                maxDelayBetweenActions = oldRes.maxDelayBetweenActionsMillis,
                                group = newGroup,
                                role = newRole
                        )
                        
                    }
                }
            }
        }
    }
    
    private fun checkExclusive(vararg paramToNames: Pair<Boolean, String>) {
        paramToNames
                .filter { it.first } // true params
                .map { it.second } // select names
                .apply {
                    if (size > 1)
                        throw IllegalStateException("Error in JsonSettings: Parameters ${joinToString(", ")} are exclusive.")
                }
    }
}