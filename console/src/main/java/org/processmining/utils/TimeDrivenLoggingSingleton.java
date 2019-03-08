package org.processmining.utils;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.descriptions.GenerationDescriptionWithNoise;
import org.processmining.models.time_driven_behavior.ResourceMapping;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;

import java.time.Instant;
import java.util.*;

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
public class TimeDrivenLoggingSingleton extends LoggingSingleton
{
    private TimeDrivenGenerationDescription description;
    protected static Random random = new Random();
    private static TimeDrivenLoggingSingleton singleton;
    private final XTimeExtension timeExtension = XTimeExtension.instance();
    private final XOrganizationalExtension organizationalExtension = XOrganizationalExtension.instance();
    private final XLifecycleExtension lifecycleExtension = XLifecycleExtension.instance();

    protected TimeDrivenLoggingSingleton(TimeDrivenGenerationDescription description)
    {
        this.description = description;
    }

    public static void init(TimeDrivenGenerationDescription description)
    {
        singleton = new TimeDrivenLoggingSingleton(description);
    }

    public static TimeDrivenLoggingSingleton timeDrivenInstance()
    {
        return singleton;
    }

    public Resource logStartEventWithResource(XTrace trace, Object modelActivity, long timeStamp)
    {
        XEvent logEvent = createEvent(modelActivity);
        putLifeCycleAttribute(logEvent, addNoiseToLifecycleProperty(false));
        Resource usedResource = setResource(modelActivity, logEvent, timeStamp);
        setTimestamp(logEvent, timeStamp);
        if (!shouldSkipEvent() && description.isSeparatingStartAndFinish())
        {
            trace.add(logEvent);
        }
        return usedResource;
    }

    public void log(XTrace trace, Object modelActivity, long timeStamp, boolean isCompleted)
    {
        if (shouldSkipEvent())
        {
            return;
        }
        XEvent logEvent = createEvent(modelActivity, timeStamp);
        putLifeCycleAttribute(logEvent, addNoiseToLifecycleProperty(isCompleted));
        if (description.isUsingResources())
        {
            setResource(modelActivity, logEvent, timeStamp);
        }
        trace.add(logEvent);
    }

    private XEvent createEvent(Object modelActivity, long timestamp)
    {
        XEvent logEvent = createEvent(modelActivity);
        setTimestamp(logEvent, timestamp);
        return logEvent;
    }

    private void setTimestamp(XEvent logEvent, long timestamp)
    {
        if (shouldDistortTimestamp())
        {
            timestamp = distortTimestamp(timestamp);
        }
        timestamp = granulateTimestamp(timestamp);
        XAttribute timeAttribute = factory
                .createAttributeTimestamp("time:timestamp", timestamp, timeExtension);
        logEvent.getAttributes().put("time:timestamp", timeAttribute);
    }

    private boolean addNoiseToLifecycleProperty(boolean original)
    {
        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = description.getNoiseDescription();
        if (description.isUsingNoise() && description.isSeparatingStartAndFinish() && noiseDescription.isUsingLifecycleNoise())
        {
            if (noiseDescription.getNoisedLevel() >= random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))  //use noise transitions
            {
                return !original;
            }
        }
        return original;
    }

    private Resource setResource(Object modelActivity, XEvent event, long timestamp)
    {
        List<Resource> availableResources = getAllResourcesMappedToActivity(modelActivity);
        Resource chosenResource = chooseAvailableResource(availableResources, timestamp);
        if (chosenResource != null)
        {
            setResource(event, chosenResource);
        }
        else
        {
            throw new IllegalStateException("Resource is null");
        }
        return chosenResource;
    }

    private Resource chooseAvailableResource(List<Resource> availableResources, long timestamp)
    {
        Resource chosenResource = null;
        if (description.isUsingSynchronizationOnResources())
        {
            while (!availableResources.isEmpty() && chosenResource == null)
            {
                int index = random.nextInt(availableResources.size());
                Resource pickedResource = availableResources.remove(index);
                if (pickedResource.isIdle() && pickedResource.getWillBeFreed() <= timestamp)
                {
                    chosenResource = pickedResource;
                    chosenResource.setIdle(false);
                }
            }
        }
        else
        {
            int index = random.nextInt(availableResources.size());
            chosenResource = availableResources.get(index);
        }
        return chosenResource;
    }

    public boolean areResourcesAvailable(Object modelActivity, long timestamp)
    {
        if (timestamp < 0)
        {
            throw new IllegalArgumentException("Time cannot be negative");
        }
        List<Resource> allResourcesMappedToActivity = getAllResourcesMappedToActivity(modelActivity);
        for (Resource resource : allResourcesMappedToActivity)
        {
            if (resource.isIdle() && resource.getWillBeFreed() <= timestamp)
            {
                return true;
            }
        }
        return false;
    }

    private List<Resource> getAllResourcesMappedToActivity(Object modelActivity)
    {
        Map<Object, ResourceMapping> generalMapping = description.getResourceMapping();
        ResourceMapping mapping = generalMapping.get(modelActivity);
        List<Resource> availableResources = new ArrayList<Resource>(mapping.getSelectedResources());
        if (!description.isUsingComplexResourceSettings())
        {
            availableResources.addAll(mapping.getSelectedSimplifiedResources());
        }
        return availableResources;
    }

    private boolean shouldDistortTimestamp()
    {
        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = description.getNoiseDescription();
        if (description.isUsingNoise() && noiseDescription.isUsingTimestampNoise())
        {
            if (noiseDescription.getNoisedLevel() >= random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))  //use noise transitions
            {
                return true;
            }
        }
        return false;
    }

    private long distortTimestamp(long originalTimestamp)
    {
        System.out.println("Timestamp is distorted");    //TODO delete?
        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = description.getNoiseDescription();

        int deviation = random.nextInt(noiseDescription.getMaxTimestampDeviation() + 1) * 1000;

        if (random.nextBoolean())
        {
            deviation = -deviation;
        }

        long resultedTimestamp = originalTimestamp + deviation;

        Instant generationStartTime = description.getGenerationStart();

        if (resultedTimestamp < generationStartTime.toEpochMilli())
        {
            resultedTimestamp = generationStartTime.toEpochMilli();
        }
        return resultedTimestamp;
    }

    private void setResource(XEvent logEvent, Resource resource)
    {
        if (description.isUsingComplexResourceSettings())
        {
            XAttribute groupAttribute = factory.createAttributeLiteral("org:group", resource.getGroup().toString(),
                    organizationalExtension);
            XAttribute roleAttribute = factory.createAttributeLiteral("org:role", resource.getRole().toString(),
                    organizationalExtension);
            XAttribute resourceExtension = factory.createAttributeLiteral("org:resource", resource.toString(),
                    organizationalExtension);

            logEvent.getAttributes().put("org:group", groupAttribute);
            logEvent.getAttributes().put("org:role", roleAttribute);
            logEvent.getAttributes().put("org:resource", resourceExtension);
        }
        else
        {
            XAttribute resourceExtension = factory.createAttributeLiteral("org:resource", resource.toString(),
                    organizationalExtension);
            logEvent.getAttributes().put("org:resource", resourceExtension);
        }
    }

    private long granulateTimestamp(long timestamp)
    {
        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = description.getNoiseDescription();

        if (description.isUsingNoise() && noiseDescription.isUsingTimeGranularity())
        {
            System.out.println("Timestamp is granulated"); //TODO delete?
            long precision = noiseDescription.getGranularityType().getPrecision();
            long modulo = timestamp % precision;

            if (modulo * 2 >= precision)
            {
                timestamp += precision - modulo;
            }
            else
            {
                timestamp -= modulo;
            }
        }
        return timestamp;
    }

    public long getNearestResourceTime(Object modelActivity)
    {
        List<Resource> resources = getAllResourcesMappedToActivity(modelActivity);
        long leastResourceTime = resources.get(0).getWillBeFreed();
        for (Resource resource : resources)
        {
            if (resource.getWillBeFreed() < leastResourceTime)
            {
                leastResourceTime = resource.getWillBeFreed();
            }
        }
        return leastResourceTime;
    }

    public void logCompleteEventWithResource(XTrace trace, Object modelActivity, Resource resource, long timeStamp)
    {
        resource.setIdle(true);
        if (shouldSkipEvent())
        {
            return;
        }
        XEvent logEvent = createEvent(modelActivity, timeStamp);
        putLifeCycleAttribute(logEvent, true);
        setResource(logEvent, resource);
        trace.add(logEvent);
    }

    private void putLifeCycleAttribute(XEvent logEvent, boolean isComplete)
    {
        if (isComplete)
        {
            putLifeCycleAttribute(logEvent, "complete");
        }
        else
        {
            putLifeCycleAttribute(logEvent, "start");
        }
    }

    private void putLifeCycleAttribute(XEvent logEvent, String transition)
    {
        XAttribute attribute = factory.createAttributeLiteral("lifecycle:transition", transition, lifecycleExtension);
        logEvent.getAttributes().put("lifecycle:transition", attribute);
    }

    private boolean shouldSkipEvent()
    {
        GenerationDescriptionWithNoise.NoiseDescription noiseDescription = description.getNoiseDescription();
        if (description.isUsingNoise() && noiseDescription.isSkippingTransitions())
        {
            if (noiseDescription.getNoisedLevel() >= random.nextInt(GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))  //use noise transitions
            {
                return true;
            }
        }
        return false;
    }
}
