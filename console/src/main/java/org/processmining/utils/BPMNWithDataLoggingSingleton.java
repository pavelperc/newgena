package org.processmining.utils;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.bpmn_with_data.LoggableDataObject;
import org.processmining.models.descriptions.DescriptionWithDataObjects;

/**
 * Created by Ivan on 30.04.2015.
 */
public class BPMNWithDataLoggingSingleton extends BPMNLoggingSingleton
{
    private static BPMNWithDataLoggingSingleton singleton;
    private DescriptionWithDataObjects description;

    protected BPMNWithDataLoggingSingleton(DescriptionWithDataObjects description, boolean isUsingResources)
    {
        super(isUsingResources);
        this.description = description;
        maskSingleton(this);
    }

    public static void init(DescriptionWithDataObjects description, boolean isUsingResources)
    {
        singleton = new BPMNWithDataLoggingSingleton(description, isUsingResources);
    }

    public static BPMNLoggingSingleton getBPMNWithDataSingleton()
    {
        return singleton;
    }

    @Override
    public void log(
            XTrace trace, Object model,
            Object group, Object resource, String parentSubProcess)
    {
        XEvent event = createEvent(model, group, resource, parentSubProcess);

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
