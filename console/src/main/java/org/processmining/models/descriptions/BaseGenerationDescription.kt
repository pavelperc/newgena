package org.processmining.models.descriptions

import org.processmining.models.GenerationDescription

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
abstract class BaseGenerationDescription(
        numberOfLogs: Int = 5, // TODO remove default values, leave only in real classes, not abstract
        numberOfTraces: Int = 10,
        maxNumberOfSteps: Int = 100
) : GenerationDescription {

//    override var numberOfLogs by Delegates.vetoable(5) { _, _, newValue ->
//        if (newValue <= 0)
//            throw IllegalArgumentException("Precondition violated in GenerationDescription." +
//                    " Number of logs must be greater than 0")
//        else true
//    }
    
    override var numberOfLogs = numberOfLogs
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Precondition violated in GenerationDescription." +
                        " Number of logs must be greater than 0")
            }
            field = value
        }
    
    
    override var numberOfTraces = numberOfTraces
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Precondition violated in GenerationDescription." +
                        " Number of traces must be greater than 0")
            }
            field = value
        }
    
    
    //переименовать steps в transitions?
    override var maxNumberOfSteps = maxNumberOfSteps
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Precondition violated in GenerationDescription." +
                        " Maximum number of steps must be greater than 0")
            }
            field = value
        }
}
