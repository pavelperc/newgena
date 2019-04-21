package org.processmining.models.descriptions

/**
 * Created by Ivan Shugurov on 30.12.2014.
 */
open class BasicBPMNGenerationDescription(
        override var isUsingResources: Boolean = false
) : BaseGenerationDescription() {
//    override var isUsingResources: Boolean = false
    
    override val isUsingTime: Boolean = false
    
    override val isUsingLifecycle: Boolean = false
    
    override val isRemovingEmptyTraces: Boolean = true
    
    override val isRemovingUnfinishedTraces: Boolean = true
}
