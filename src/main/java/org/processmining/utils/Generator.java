package org.processmining.utils;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;
import org.processmining.models.AssessedMovementResult;
import org.processmining.models.GenerationDescription;
import org.processmining.models.Movable;
import org.processmining.models.MovementResult;
import org.processmining.utils.helpers.GenerationHelper;

/**
 * @author Ivan Shugurov
 *         Created on 25.11.2013
 */
public class Generator
{
    private XFactory factory = new XFactoryBufferedImpl();
    private ProgressBarCallback callback;

    public Generator(ProgressBarCallback callback)
    {
        this.callback = callback;
    }

    public EventLogArray generate(GenerationHelper generationHelper)
    {
        return generateLogs(generationHelper);
    }


    private EventLogArray generateLogs(GenerationHelper generationHelper)
    {
        EventLogArray logArray = EventLogArrayFactory.createEventLogArray();
        GenerationDescription generationDescription = generationHelper.getGenerationDescription();
        for (int logNumber = 0; logNumber < generationDescription
                .getNumberOfLogs(); logNumber++)
        {
            System.out.println("Log number " + (logNumber + 1));
            XLog generatedLog = generateLog(generationHelper);
            logArray.addLog(generatedLog);
        }
        return logArray;
    }

    private XLog generateLog(GenerationHelper generationHelper)
    {
        XLog log = factory.createLog();
        GenerationDescription generationDescription = generationHelper.getGenerationDescription();

        XConceptExtension conceptExtension = XConceptExtension.instance();
        log.getExtensions().add(conceptExtension);
        log.getGlobalEventAttributes().addAll(conceptExtension.getEventAttributes());
        log.getGlobalTraceAttributes().addAll(conceptExtension.getTraceAttributes());

        if (generationDescription.isUsingTime())
        {
            log.getExtensions().add(XTimeExtension.instance());
        }

        if (generationDescription.isUsingLifecycle())
        {
            XLifecycleExtension lifecycleExtension = XLifecycleExtension.instance();
            log.getExtensions().add(lifecycleExtension);
            log.getGlobalEventAttributes().addAll(lifecycleExtension.getEventAttributes());
        }

        if (generationDescription.isUsingResources())
        {
            XOrganizationalExtension organizationalExtension = XOrganizationalExtension.instance();
            log.getExtensions().add(organizationalExtension);
            log.getGlobalEventAttributes().addAll(organizationalExtension.getEventAttributes());
        }

        if (generationDescription.isUsingTime())
        {
            XTimeExtension timeExtension = XTimeExtension.instance();
            log.getExtensions().add(timeExtension);
            log.getGlobalEventAttributes().addAll(timeExtension.getEventAttributes());
        }

        int maxAttempt = 2;

        for (int i = 0; i < generationDescription.getNumberOfTraces(); i++)
        {
            String traceName = "Trace " + Integer.toString(i + 1);
            System.out.println(traceName);
            XTrace generatedTrace = generateTrace(generationHelper, traceName);
            boolean successful = addTraceToLog(log, generatedTrace, generationDescription);

            if (maxAttempt == 0)
            {
                maxAttempt = 2;
                continue;
            }

            if (!successful)
            {
                maxAttempt--;
                i--;
            }
        }
        return log;
    }

    private boolean addTraceToLog(XLog log, XTrace generatedTrace, GenerationDescription generationDescription)
    {
        if (generatedTrace == null)
        {
            return false;
        }
        else
        {
            boolean isCorrectTrace = true;

            if (generationDescription.isRemovingEmptyTraces() && generatedTrace.isEmpty())
            {
                isCorrectTrace = false;
            }
            else
            {
                log.add(generatedTrace);
                callback.increment();
            }

            return isCorrectTrace;
        }

    }

    private <K extends Movable, F extends Movable> XTrace generateTrace(GenerationHelper<K, F> generationHelper, String traceName)
    {
        XTrace trace;

        boolean replayedCompletely = false;
        boolean addTraceToLog = true;

        GenerationDescription generationDescription = generationHelper.getGenerationDescription();

        int maxIterations = 10;

        do
        {
            generationHelper.moveToInitialState();
            trace = createTrace(traceName);
            int stepNumber = 0;

            while (stepNumber < generationDescription.getMaxNumberOfSteps() && !replayedCompletely)
            {
                Movable movable = generationHelper.chooseNextMovable();

                if (movable == null)
                {
                    trace = null;
                    break;
                }

                MovementResult movementResult = movable.move(trace);

                if (!movementResult.isActualStep())
                {
                    stepNumber--;
                }

                AssessedMovementResult assessedMovementResult = generationHelper.handleMovementResult(movementResult);
                replayedCompletely = assessedMovementResult.isReplayCompleted();
                addTraceToLog = assessedMovementResult.isTraceEligibleForAddingToLog();
                stepNumber++;
            }

            maxIterations--;
        }
        while (maxIterations > 0 && (!addTraceToLog || (generationDescription.isRemovingUnfinishedTraces() && !replayedCompletely)));

        return trace;
    }

    private XTrace createTrace(String traceName)
    {
        XTrace trace;
        trace = factory.createTrace();
        XAttribute name = factory.createAttributeLiteral("concept:name", traceName,
                XConceptExtension.instance());
        trace.getAttributes().put("concept:name", name);
        return trace;
    }

}
