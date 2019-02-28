package org.processmining.models.descriptions

/**
 * Created by Ivan Shugurov on 12.11.2014.
 */
class SimpleGenerationDescription(
        numberOfLogs: Int = 5,
        numberOfTraces: Int = 10,
        maxNumberOfSteps: Int = 100,
        isUsingNoise: Boolean = false,
        isRemovingUnfinishedTraces: Boolean = true,
        isRemovingEmptyTraces: Boolean = true,
        noiseDescriptionCreator: GenerationDescriptionWithNoise.() -> NoiseDescription = { this.NoiseDescription() }

) : GenerationDescriptionWithNoise(numberOfLogs, numberOfTraces, maxNumberOfSteps, isUsingNoise, isRemovingUnfinishedTraces, isRemovingEmptyTraces, noiseDescriptionCreator) {
    override val isUsingTime: Boolean = false
    override val isUsingResources: Boolean = false
    override val isUsingLifecycle: Boolean = false
}