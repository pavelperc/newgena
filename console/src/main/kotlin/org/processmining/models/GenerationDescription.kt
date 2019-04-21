package org.processmining.models

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
interface GenerationDescription {
    var numberOfLogs: Int
    
    var numberOfTraces: Int
    
    var maxNumberOfSteps: Int
    
    val isUsingTime: Boolean
    
    val isUsingResources: Boolean
    
    val isUsingLifecycle: Boolean
    
    val isRemovingEmptyTraces: Boolean
    
    val isRemovingUnfinishedTraces: Boolean
}
