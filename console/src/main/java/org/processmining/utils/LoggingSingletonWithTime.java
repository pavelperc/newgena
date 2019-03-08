package org.processmining.utils;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;

/**
 * Created by Ivan on 16.09.2015.
 */
public class LoggingSingletonWithTime extends LoggingSingleton
{
    protected static final XLifecycleExtension lifecycleExtension = XLifecycleExtension.instance();
    protected static final XTimeExtension timeExtension = XTimeExtension.instance();
    protected static final XOrganizationalExtension organizationalExtension = XOrganizationalExtension.instance();

    public static void log(BPMNWithTimeGenerationDescription description, XTrace trace, String eventName, long timestamp, boolean isComplete, String group, String resource)
    {
        XEvent event = createEvent(description, eventName, timestamp, isComplete, group, resource);

        trace.add(event);
    }

    protected static XEvent createEvent(BPMNWithTimeGenerationDescription description, String eventName, long timestamp,
                                        boolean isComplete, String group, String resource)
    {
        XEvent event = factory.createEvent();
        conceptExtension.assignName(event, eventName);

        if (description.isUsingResources())
        {
            organizationalExtension.assignResource(event, resource);
            organizationalExtension.assignGroup(event, group);
        }

        if (isComplete)
        {
            lifecycleExtension.assignStandardTransition(event, XLifecycleExtension.StandardModel.COMPLETE);
        }
        else
        {
            lifecycleExtension.assignStandardTransition(event, XLifecycleExtension.StandardModel.START);
        }

        timeExtension.assignTimestamp(event, timestamp);

        return event;
    }
}
