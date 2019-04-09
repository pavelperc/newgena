package org.processmining.utils

import com.pavelperc.newgena.utils.xlogutils.eventNames
import com.pavelperc.newgena.utils.xlogutils.name
import org.deckfour.xes.extension.std.XConceptExtension
import org.deckfour.xes.extension.std.XLifecycleExtension
import org.deckfour.xes.extension.std.XOrganizationalExtension
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.factory.XFactory
import org.deckfour.xes.factory.XFactoryBufferedImpl
import org.deckfour.xes.model.XAttribute
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.processmining.log.models.EventLogArray
import org.processmining.log.models.impl.EventLogArrayFactory
import org.processmining.models.AssessedMovementResult
import org.processmining.models.GenerationDescription
import org.processmining.models.Movable
import org.processmining.models.MovementResult
import org.processmining.utils.helpers.GenerationHelper
import org.processmining.utils.helpers.PetriNetGenerationHelper

/**
 * @author Ivan Shugurov
 * Created on 25.11.2013
 */
class Generator(private val callback: ProgressBarCallback) {
    private val factory = XFactoryBufferedImpl()
    
    fun generate(generationHelper: GenerationHelper<out Movable, out Movable>): EventLogArray = generateLogs(generationHelper)
    
    
    private fun generateLogs(generationHelper: GenerationHelper<out Movable, out Movable>): EventLogArray {
        val logArray = EventLogArrayFactory.createEventLogArray()
        val generationDescription = generationHelper.generationDescription
        
        for (logNumber in 0 until generationDescription.numberOfLogs) {
            //            System.out.println("Log number " + (logNumber + 1));
            val generatedLog = generateLog(generationHelper)
            logArray.addLog(generatedLog)
        }
        return logArray
    }
    
    private fun generateLog(generationHelper: GenerationHelper<out Movable, out Movable>): XLog {
        val log = factory.createLog()
        val generationDescription = generationHelper.generationDescription
        
        val conceptExtension = XConceptExtension.instance()
        log.extensions.add(conceptExtension)
        log.globalEventAttributes.addAll(conceptExtension.eventAttributes)
        log.globalTraceAttributes.addAll(conceptExtension.traceAttributes)
        
        if (generationDescription.isUsingTime) {
            log.extensions.add(XTimeExtension.instance())
        }
        
        if (generationDescription.isUsingLifecycle) {
            val lifecycleExtension = XLifecycleExtension.instance()
            log.extensions.add(lifecycleExtension)
            log.globalEventAttributes.addAll(lifecycleExtension.eventAttributes)
        }
        
        if (generationDescription.isUsingResources) {
            val organizationalExtension = XOrganizationalExtension.instance()
            log.extensions.add(organizationalExtension)
            log.globalEventAttributes.addAll(organizationalExtension.eventAttributes)
        }
        
        if (generationDescription.isUsingTime) {
            val timeExtension = XTimeExtension.instance()
            log.extensions.add(timeExtension)
            log.globalEventAttributes.addAll(timeExtension.eventAttributes)
        }
        
        var maxAttempt = 2
        
        var i = 0
        while (i < generationDescription.numberOfTraces) {
            val traceName = "Trace ${i + 1}"
//            println(traceName)
            
            val generatedTrace = generateTrace(generationHelper, traceName)
            val successful = addTraceToLog(log, generatedTrace, generationDescription)
            
            if (maxAttempt == 0) {
                maxAttempt = 2
                i++
                continue
            }
            
            if (!successful) {
                maxAttempt--
                i--
            }
            i++
        }
        return log
    }
    
    private fun addTraceToLog(log: XLog, generatedTrace: XTrace?, generationDescription: GenerationDescription): Boolean {
        if (generatedTrace == null) {
            return false
        } else {
            var isCorrectTrace = true
            
            if (generationDescription.isRemovingEmptyTraces && generatedTrace.isEmpty()) {
                isCorrectTrace = false
            } else {
                log.add(generatedTrace)
                callback.increment()
            }
            
            return isCorrectTrace
        }
        
    }
    
    private fun <K : Movable, F : Movable> generateTrace(generationHelper: GenerationHelper<K, F>, traceName: String): XTrace? {
        var trace: XTrace?
        
        var replayedCompletely = false
        var addTraceToLog = true
        
        val generationDescription = generationHelper.generationDescription
        
        var maxIterations = 10
        println("New trace")
        do {
            println("New iteration.")
            generationHelper.moveToInitialState()
            trace = createTrace(traceName)
            var stepNumber = 0
    
            (generationHelper as? PetriNetGenerationHelper<*,*,*>)?.dumpPetrinet()
            while (stepNumber < generationDescription.maxNumberOfSteps && !replayedCompletely) {
                
                val movable = generationHelper.chooseNextMovable()
                
                if (movable == null) {
                    trace = null
                    break
                }
                
                // TODO pavel: update movable interface return type and implementations 
                // can add noise tokens. 
                val movementResult = movable.move(trace!!) as MovementResult<F>
                
                if (!movementResult.isActualStep) {
                    stepNumber--
                }
                // check if we reached final marking, produce or consume extra tokens??
                val assessedMovementResult = generationHelper.handleMovementResult(movementResult)
                replayedCompletely = assessedMovementResult.isReplayCompleted
                addTraceToLog = assessedMovementResult.isTraceEligibleForAddingToLog
                stepNumber++
                
                if (replayedCompletely) {
                    println("In final Marking!!")
                }
                
                (generationHelper as? PetriNetGenerationHelper<*,*,*>)?.dumpPetrinet()
            }
            
            maxIterations--
        } while (maxIterations > 0 && (!addTraceToLog || generationDescription.isRemovingUnfinishedTraces && !replayedCompletely))
        
        if (!replayedCompletely && generationDescription.isRemovingUnfinishedTraces)
            return null
        return trace
    }
    
    private fun createTrace(traceName: String): XTrace {
        val trace = factory.createTrace()
        trace.name = traceName
        return trace
    }
    
}
