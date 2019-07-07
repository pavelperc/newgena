package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.loaders.settings.jsonSettings.*
import com.pavelperc.newgena.models.pnmlId
import com.pavelperc.newgena.utils.common.markingOf
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.*
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.organizational_extension.Role
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.time_driven_behavior.NoiseEvent
import org.processmining.models.time_driven_behavior.ResourceMapping

class JsonSettingsBuilder(val petrinet: PetrinetGraph, val jsonSettings: JsonSettings) {
    
    private val idsToTransitions = petrinet.transitions.map { it.pnmlId to it!! }.toMap()
    
    private fun String.toTrans() = idsToTransitions[this]
            ?: throw IllegalStateException("Not found transition id $this.")
    
    
    fun buildDescription() = jsonSettings.build()
    
    
    companion object {
        /** Builds a marking, not using the whole [JsonSettings], but only [JsonMarking] part.
         * @return a pair of initialMarking and finalMarking.*/
        fun buildMarkingOnly(marking: JsonMarking, petrinet: PetrinetGraph) =
                marking.run {
                    val idsToPlaces = petrinet.places.map { it.pnmlId to it!! }.toMap()
                    
                    val initialMarking = initialPlaceIds.mapKeys { (id, _) -> idsToPlaces.getValue(id) }
                    val finalMarking = finalPlaceIds.mapKeys { (id, _) -> idsToPlaces.getValue(id) }
                    
                    markingOf(initialMarking) to markingOf(finalMarking)
                }
    }
    
    /** See also [JsonSettingsBuilder.buildMarkingOnly].
     * @return a pair of initialMarking and finalMarking.*/
    fun buildMarking(): Pair<Marking, Marking> = buildMarkingOnly(jsonSettings.petrinetSetup.marking, petrinet)
    
    private fun JsonSettings.build(): GenerationDescription {
        val description: GenerationDescription
        
        checkExclusive(isUsingTime to "isUsingTime", isUsingStaticPriorities to "isUsingStaticPriorities")
        checkExclusive(isUsingStaticPriorities to "isUsingStaticPriorities", isUsingNoise to "isUsingNoise")
        
        
        if (isUsingStaticPriorities) {
            description = staticPriorities.run {
                GenerationDescriptionWithStaticPriorities(
                        numberOfLogs = numberOfLogs,
                        numberOfTraces = numberOfTraces,
                        maxNumberOfSteps = maxNumberOfSteps,
                        isRemovingUnfinishedTraces = isRemovingUnfinishedTraces,
                        isRemovingEmptyTraces = isRemovingEmptyTraces,
                        priorities = transitionIdsToPriorities.mapKeys { it.key.toTrans() }
                )
            } // ?: throw IllegalStateException("staticPriorities is null, but isUsingStaticPriorities is true.")
        } else {
            
            val noiseDescriptionCreator = if (isUsingNoise) {
                noiseDescription.build()
                // ?: throw IllegalStateException("noiseDescription is null, but isUsingNoise is true.")
            } else {
                { NoiseDescription() }
            }
            
            if (isUsingTime) {
                
                description = timeDescription.build(this)
                // ?: throw IllegalStateException("timeDescription is null, but isUsingTime is true.")
                
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
                
                // building resources
                simpleRes = simplifiedResources.map { buildSimplified(it) }
                resGroups = resourceGroups.map { it.build() }
                
                
                
                
                
                resMapping = buildResourceMapping(
                        simpleRes,
                        resGroups,
                        transitionIdsToResources,
                        jsonSettings.noiseDescription.artificialNoiseEvents,
                        allowArtificial = jsonSettings.isUsingNoise
                                && jsonSettings.noiseDescription.isUsingExternalTransitions
                )
            }
            
            
            val timeNoiseDescriptionCreator: TimeNoiseDescriptionCreator
            if (isUsingNoise) {
                val commonNoise = noiseDescription
                //?: throw IllegalStateException("noiseDescription is null, but isUsingNoise is true.")
                val timeNoise = timeDrivenNoise
                //?: throw IllegalStateException("timeDrivenNoise is null, but isUsingNoise is true.")
                
                timeNoiseDescriptionCreator = timeNoise.build(commonNoise)
            } else {
                timeNoiseDescriptionCreator = { TimeNoiseDescription() }
            }
            
            
            TimeDrivenGenerationDescription(
                    numberOfLogs = numberOfLogs,
                    numberOfTraces = numberOfTraces,
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
                    resourceMapping = resMapping, // transitions and noise events to resources.
                    resourceGroups = resGroups,
                    noiseDescriptionCreator = timeNoiseDescriptionCreator
            )
        }
    }
    
    private fun buildResourceMapping(
            simpleRes: List<Resource>, // already built.
            resGroups: List<Group>,
            transitionIdsToResources: MutableMap<String, JsonResources.JsonResourceMapping>,
            artificialNoiseEvents: List<NoiseEvent>,
            allowArtificial: Boolean
    ): Map<Any, ResourceMapping> {
        
        val complexRes = resGroups.flatMap { it.resources }
        val resRoles = resGroups.flatMap { it.roles }
        
        // mappings from names to resources:
        val simpleResFromNames = simpleRes.map { it.name to it }.toMap()
        
        val complexResFromNames = complexRes.map { it.name to it }.toMap()
        val complexResFromRoleNames = resRoles.map { it.name to it.resources }.toMap()
        val complexResFromGroupNames = resGroups.map { it.name to it.resources }.toMap()
        
        // mapping for artificial noise event
        val noiseEventsFromNames = artificialNoiseEvents
                .map { it.activity.toString() to it }.toMap()
        
        // transition id and artificial noise name collisions
        val transNoiseCollisions = noiseEventsFromNames.keys.intersect(idsToTransitions.keys)
        if (transNoiseCollisions.isNotEmpty() && allowArtificial) {
            throw IllegalStateException("Building resource mapping: " +
                    "Found collisions among transitionIds and ArtificialNoiseEvent names: $transNoiseCollisions.")
        }
        
        
        return transitionIdsToResources
                .filterKeys { allowArtificial || it !in noiseEventsFromNames }
                .mapValues { (transId, jsonMapping) ->
                    // split all names into simple and complex resources
                    
                    fun unknownRes(type: String, name: String) = IllegalStateException("Building resource mapping: " +
                            "Unknown $type name for transitionId/artificialEvent $transId: $name.")
                    
                    val mappedSimpleRes = jsonMapping.simplifiedResourceNames.map { name ->
                        simpleResFromNames[name] ?: throw unknownRes("simplified resource", name)
                    }
                    
                    // map from complex groups, roles, complex names and collect together 
                    val mappedComplexRes = listOf(
                            jsonMapping.resourceGroups.flatMap { name ->
                                // list of res for one group
                                complexResFromGroupNames[name] ?: throw unknownRes("resource group", name)
                            },
                            jsonMapping.resourceRoles.flatMap { name ->
                                // list of res for one role
                                complexResFromRoleNames[name] ?: throw unknownRes("resource role", name)
                            },
                            jsonMapping.complexResourceNames.map { name ->
                                // a res
                                complexResFromNames[name] ?: throw unknownRes("complex resource", name)
                            }
                    )
                            .flatten()
                            .distinct()
                    
                    ResourceMapping(
                            selectedSimplifiedResources = mappedSimpleRes,
                            selectedResources = mappedComplexRes
                    )
                }
                .mapKeys { (transId, _) ->
                    idsToTransitions[transId]
                            ?: noiseEventsFromNames[transId]
                            ?: throw IllegalStateException("Building Resource mapping: " +
                                    "id $transId not found among transition ids and artificial noise events (if enabled).")
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
                artificialNoiseEvents = artificialNoiseEvents
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
                        artificialNoiseEvents = artificialNoiseEvents
                )
            }
        }
    }
    
    /** Builds resource only by name. */
    private fun buildSimplified(resourceName: String) = Resource.simplified(resourceName)
    
    private fun JsonResources.Group.build(): Group {
        // we create roles and resources in special lambdas - creators,
        // because all roles need groups, all resources need roles and vice versa
        
        return Group(name) { newGroup ->
            roles.map { jsonRole ->
                Role(
                        name = jsonRole.name,
                        group = newGroup
                ) { newRole ->
                    jsonRole.resources.map { jsonRes ->
                        Resource(
                                name = jsonRes.name,
                                minDelayBetweenActions = jsonRes.minDelayBetweenActionsMillis,
                                maxDelayBetweenActions = jsonRes.maxDelayBetweenActionsMillis,
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