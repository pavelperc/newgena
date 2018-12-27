package org.processmining.utils;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.bpmn_with_data.LoggableDataObject;
import org.processmining.models.descriptions.BPMNWithTimeAndDataDescription;

/**
 * Created by Ivan on 24.02.2016.
 */
public class LoggingSingletonWithTimeAndData extends LoggingSingletonWithTime
{
    public static void log(
            BPMNWithTimeAndDataDescription description, XTrace trace,
            String eventName, long timestamp, boolean isComplete,
            String group, String resource)
    {
        XEvent event = createEvent(description, eventName, timestamp, isComplete, group, resource);

        XAttributeList listAttribute = factory.createAttributeList("data", null);

        for (LoggableDataObject dataObject : description.getDataObjects())
        {
            String dataObjectName = dataObject.getLabel();
            String value = dataObject.read().toString();

            XAttribute attribute = factory.createAttributeLiteral(dataObjectName, value, null);
            listAttribute.addToCollection(attribute);
        }

        event.getAttributes().put("", listAttribute);

        trace.add(event);
    }
}
