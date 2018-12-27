package org.processmining.utils;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

/**
 * @author Ivan Shugurov
 *         Created on 03.08.2014
 */
public class LoggingSingleton
{
    protected static XFactory factory = new XFactoryBufferedImpl();
    protected static XConceptExtension conceptExtension = XConceptExtension.instance();


    public static void log(XTrace trace, Object modelActivity)
    {
        XEvent event = createEvent(modelActivity);
        trace.add(event);
    }

    protected static XEvent createEvent(Object modelActivity)
    {
        XEvent logEvent = factory.createEvent();
        conceptExtension.assignName(logEvent, modelActivity.toString());

        return logEvent;
    }

}
