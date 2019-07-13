package org.processmining.utils

import com.pavelperc.newgena.utils.xlogutils.name
import org.deckfour.xes.extension.std.XConceptExtension
import org.deckfour.xes.extension.std.XLifecycleExtension
import org.deckfour.xes.extension.std.XOrganizationalExtension
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.factory.XFactoryBufferedImpl
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.processmining.log.models.EventLogArray
import org.processmining.log.models.impl.EventLogArrayFactory
import org.processmining.models.GenerationDescription
import org.processmining.models.Movable
import org.processmining.models.MovementResult
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.utils.helpers.GenerationHelper
import org.processmining.utils.helpers.PetriNetGenerationHelper


typealias GenerationCallback = (progress: Int, maxProgress: Int) -> Unit

val emptyCallback: GenerationCallback = { _, _ -> }

/** returns callback, normed by 100. */
fun percentCallBack(callback: GenerationCallback) : GenerationCallback {
    var oldPercents = 0
    return { progress, maxProgress ->
        val percents = progress * 100 / maxProgress
        if (percents != oldPercents) {
            callback(percents, 100)
        }
        oldPercents = percents
    }
}

/**
 * @author Ivan Shugurov
 * Created on 25.11.2013
 */
class Generator(
        private val generationHelper: GenerationHelper<out Movable, out Movable>,
        private val callback: GenerationCallback = { _, _ -> }
) {
    private val factory = XFactoryBufferedImpl()
    
    
    private val generationDescription = generationHelper.generationDescription
    
    private var progress = 0
    private val maxProgress = generationDescription.numberOfLogs * generationDescription.numberOfTraces
    
    private fun incrementCallback() {
        callback(progress++, maxProgress)
    }
    
    
    fun generate(): EventLogArray {
        val logArray = EventLogArrayFactory.createEventLogArray()
        val generationDescription = generationHelper.generationDescription
        
        for (logNumber in 0 until generationDescription.numberOfLogs) {
            val generatedLog = generateLog()
            logArray.addLog(generatedLog)
        }
        return logArray
    }
    
    private fun generateLog(): XLog {
        val log = factory.createLog()
        
        val conceptExtension = XConceptExtension.instance()
        log.extensions.add(conceptExtension)
        log.globalEventAttributes.addAll(conceptExtension.eventAttributes)
        log.globalTraceAttributes.addAll(conceptExtension.traceAttributes)
        
        if (generationDescription.isUsingTime) {
            TimeDrivenLoggingSingleton.init(generationDescription as TimeDrivenGenerationDescription)
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
    
    /** Checks if the trace is not null or not empty and adds it to the log. */
    private fun addTraceToLog(log: XLog, generatedTrace: XTrace?, generationDescription: GenerationDescription): Boolean {
        if (generatedTrace == null) {
            return false
        } else {
            var isCorrectTrace = true
            
            if (generationDescription.isRemovingEmptyTraces && generatedTrace.isEmpty()) {
                isCorrectTrace = false
            } else {
                log.add(generatedTrace)
                incrementCallback()
            }
            
            return isCorrectTrace
        }
        
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <K : Movable, F : Movable> generateTrace(generationHelper: GenerationHelper<K, F>, traceName: String): XTrace? {
        var trace: XTrace?
        
        var replayedCompletely = false
        var addTraceToLog = true
        
        val generationDescription = generationHelper.generationDescription
        
        var maxIterations = 10
//        println("New trace")
        
        fun dumpPetrinet(moreText: String = "") {
            (generationHelper as? PetriNetGenerationHelper<*, *, *>)?.dumpPetrinet(moreText)
        }
        
        do {
//            println("New iteration.")
            generationHelper.moveToInitialState()
            trace = createTrace(traceName)
            var stepNumber = 0

//            dumpPetrinet("before trace")
            while (stepNumber < generationDescription.maxNumberOfSteps && !replayedCompletely) {
                
                val movable = generationHelper.chooseNextMovable()
                
                if (movable == null) {
                    trace = null
                    break
                }
                
                // can add noise tokens. 
                val movementResult = movable.move(trace!!) as MovementResult<F>
                
                if (!movementResult.isActualStep) {
                    stepNumber--
                }
//                dumpPetrinet("event=${trace.lastOrNull()?.name}, step = $stepNumber, isActual=${movementResult.isActualStep}")
                
                // check if we reached final marking, produce or consume extra tokens??
                // костыли какие-то со всеми этими extraTokens. 
                val assessedMovementResult = generationHelper.handleMovementResult(movementResult)
                replayedCompletely = assessedMovementResult.isReplayCompleted
                addTraceToLog = assessedMovementResult.isTraceEligibleForAddingToLog
                stepNumber++
                
                
                if (Thread.interrupted()) {
                    throw InterruptedException("Interrupted generation after ${trace.size} events in a trace.")
                }
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
