package org.processmining.utils;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

/**
 * Created by Ivan on 18.03.2015.
 */
public class BPMNLoggingSingleton extends LoggingSingleton
{
    private static BPMNLoggingSingleton singleton;
    protected XOrganizationalExtension organizationalExtension;
    private boolean isUsingResources;

    protected BPMNLoggingSingleton(boolean isUsingResources)
    {
        this.isUsingResources = isUsingResources;

        if (isUsingResources)
        {
            organizationalExtension = XOrganizationalExtension.instance();
        }
    }

    public static BPMNLoggingSingleton getBPMNSingleton()
    {
        return singleton;
    }

    public static void init(boolean isUsingResources)
    {
        singleton = new BPMNLoggingSingleton(isUsingResources);
    }

    public void log(XTrace trace, Object model, Object group, Object resource, String parentSubProcess)
    {
        XEvent event = createEvent(model, group, resource, parentSubProcess);
        trace.add(event);
    }

    protected XEvent createEvent(Object model, Object group, Object resource, String parentSubProcess)
    {
        XEvent event = createEvent(model);

        if (isUsingResources)
        {
            if(resource != null)
            {
                organizationalExtension.assignResource(event, resource.toString());
            }

            if (group != null)
            {
                organizationalExtension.assignGroup(event, group.toString());
            }
        }

        if (parentSubProcess != null)
        {
            XAttributeLiteral literal = new XAttributeLiteralImpl("subprocess", parentSubProcess);
            event.getAttributes().put("subprocess", literal);
        }

        return event;
    }

    public XOrganizationalExtension getOrganizationalExtension()
    {
        return organizationalExtension;
    }

    protected void maskSingleton(BPMNLoggingSingleton newSingleton)
    {
        singleton = newSingleton;
    }

}
