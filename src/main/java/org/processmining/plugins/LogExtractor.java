package org.processmining.plugins;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.hub.ProMResourceManager;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.log.models.EventLogArray;
import ru.hse.pais.shugurov.widgets.panels.MultipleChoicePanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan Shugurov
 *         Created on 24.02.2014
 */

public class LogExtractor
{
    @Plugin(
            name = "GENA: Extract log from array",
            returnTypes = {},
            returnLabels = {},
            parameterLabels = "LogArray",
            userAccessible = true,
            mostSignificantResult = -1//needed to make a plug-in without a return value
    )
    @UITopiaVariant
            (
                    affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov"
            )
    public void extract(UIPluginContext context, EventLogArray array)
    {
        List<XLogWrapper> logs = new ArrayList<XLogWrapper>(array.getSize());
        for (int i = 0; i < array.getSize(); i++)
        {
            logs.add(new XLogWrapper(array.getLog(i)));
        }
        MultipleChoicePanel<XLogWrapper> panel = new MultipleChoicePanel<XLogWrapper>(logs);
        TaskListener.InteractionResult interactionResult = context.showWizard("Choose event logs to be extracted", true, true, panel);
        switch (interactionResult)
        {
            case FINISHED:
                Set<XLogWrapper> chosenLogs = panel.getChosenOptionsAsSet();
                ProvidedObjectManager objectManager = context.getProvidedObjectManager();
                ProMResourceManager resourceManager = context.getGlobalContext().getResourceManager();
                for (XLogWrapper logWrapper : chosenLogs)
                {
                    objectManager.createProvidedObject("Generated event log", logWrapper.getXLog(), XLog.class, context);
                    resourceManager.getResourceForInstance(logWrapper.getXLog()).setFavorite(true);
                }
                break;
        }
    }

    public class XLogWrapper
    {
        private XLog log;
        private int minNumberOfEvents;
        private int maxNumberOfEvents;
        private int numberOfTraces;

        public XLogWrapper(XLog log)
        {
            this.log = log;
            numberOfTraces = log.size();
            if (numberOfTraces != 0)
            {
                minNumberOfEvents = log.get(0).size();
                maxNumberOfEvents = log.get(0).size();
                for (int i = 1; i < log.size(); i++)
                {
                    XTrace trace = log.get(i);
                    int numberOfEvents = trace.size();
                    if (minNumberOfEvents > numberOfEvents)
                    {
                        minNumberOfEvents = numberOfEvents;
                    }
                    if (maxNumberOfEvents < numberOfEvents)
                    {
                        maxNumberOfEvents = numberOfEvents;
                    }

                }
            }
        }

        @Override
        public String toString()
        {
            return "Number of traces: " + numberOfTraces + "; Number of events: " + " min " + minNumberOfEvents + ", max: " + maxNumberOfEvents;
        }

        public XLog getXLog()
        {
            return log;
        }
    }
}
