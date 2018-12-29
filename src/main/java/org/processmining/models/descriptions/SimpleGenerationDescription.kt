package org.processmining.models.descriptions

/**
 * Created by Ivan Shugurov on 12.11.2014.
 */
class SimpleGenerationDescription : GenerationDescriptionWithNoise() {
    
    override val isUsingTime: Boolean = false
    
    override val isUsingResources: Boolean = false
    
    override val isUsingLifecycle: Boolean = false
    
}
