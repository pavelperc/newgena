package org.processmining.models.time_driven_behavior;

import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Transition;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.utils.TimeDrivenLoggingSingleton;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class TimeDrivenTransition extends Transition<TimeDrivenPlace> implements Comparable<TimeDrivenTransition>
{
    private static final int NOISE_INSTEAD_OF_ACTUAL_EVENT = 2;
    private static final int NOISE_BEFORE_ACTUAL_EVENT = 0;
    private static final int NOISE_AFTER_ACTUAL_EVENT = 1;

    private long executionTime;
    private long maxTimeDeviation;

    protected TimeDrivenTransition(org.processmining.models.graphbased.directed.petrinet.elements.Transition node, TimeDrivenGenerationDescription generationDescription, TimeDrivenPlace[] inputPlaces, TimeDrivenPlace[] outputPlaces)
    {
        super(node, generationDescription, inputPlaces, outputPlaces);
        Pair<Long, Long> timePair = generationDescription.getTime().get(node);
        executionTime = timePair.getFirst();
        maxTimeDeviation = timePair.getSecond();
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult<TimeDrivenToken> movementResult = new MovementResult<TimeDrivenToken>();
        movementResult.setActualStep(false);
        long time = findMinimalTokenTime(); //TODO такой способ нахождения времени не оптимален
        if (getGenerationDescription().isUsingSynchronizationOnResources() && !TimeDrivenLoggingSingleton.timeDrivenInstance().areResourcesAvailable(getNode(), time))
        {
            takeSynchronizationStep(movementResult);
            return movementResult;
        }
        boolean tokensHaveTheSameTimestamp = checkTimeOfTokens();

        if (tokensHaveTheSameTimestamp)
        {
            if (shouldDistortEvent())
            {
                int distortionType = getDistortionType();

                switch (distortionType)
                {
                    case NOISE_BEFORE_ACTUAL_EVENT:
                        System.out.println("Noise before actual event: " + getNode());//TODO delete?
                        registerNoiseTransition(trace, time, movementResult);
                        if (getGenerationDescription().isUsingSynchronizationOnResources())
                        {
                            if (TimeDrivenLoggingSingleton.timeDrivenInstance().areResourcesAvailable(getNode(), time))
                            {
                                actuallyMove(trace, movementResult);
                            }
                            else
                            {
                                takeSynchronizationStep(movementResult);
                            }
                        }
                        else
                        {
                            actuallyMove(trace, movementResult);
                        }
                        break;
                    case NOISE_AFTER_ACTUAL_EVENT:
                        System.out.println("Noise after actual event: " + getNode()); //TODO delete?
                        actuallyMove(trace, movementResult);
                        registerNoiseTransition(trace, time, movementResult);
                        break;
                    case NOISE_INSTEAD_OF_ACTUAL_EVENT:
                        System.out.println("Noise instead of actual event: " + getNode()); //TODO delete?
                        MovementResult<TimeDrivenToken> extraResult = new MovementResult<TimeDrivenToken>();
                        extraResult.setActualStep(false);
                        Pair<Object, Resource> registeredPair = registerNoiseTransition(trace, time, extraResult);
                        if (registeredPair == null)
                        {
                            if (getGenerationDescription().isUsingSynchronizationOnResources())
                            {
                                if (TimeDrivenLoggingSingleton.timeDrivenInstance().areResourcesAvailable(getNode(), time))
                                {
                                    actuallyMove(trace, movementResult);
                                }
                                else
                                {
                                    takeSynchronizationStep(movementResult);
                                }
                            }
                            else
                            {
                                actuallyMove(trace, movementResult);
                            }
                        }
                        else
                        {
                            consumeTokens();
                            List<TimeDrivenToken> createdTokens = extraResult.getProducedExtraMovables();
                            TimeDrivenToken noiseToken = createdTokens.get(0);
                            long noiseTimestamp = noiseToken.getTimestamp();
                            ReplacementToken replacementToken = new ReplacementToken(getGenerationDescription(), this, registeredPair.getFirst(), registeredPair.getSecond(), noiseTimestamp);
                            movementResult.addProducedExtraToken(replacementToken);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Incorrect type of noise " + distortionType);
                }
            }
            else
            {
                if (getGenerationDescription().isUsingSynchronizationOnResources())
                {
                    if (TimeDrivenLoggingSingleton.timeDrivenInstance().areResourcesAvailable(getNode(), time))
                    {
                        actuallyMove(trace, movementResult);
                    }
                    else
                    {
                        takeSynchronizationStep(movementResult);
                    }
                }
                else
                {
                    actuallyMove(trace, movementResult);
                }
            }
        }
        else
        {
            takeSynchronizationStep(movementResult);
        }
        return movementResult;
    }

    private void takeSynchronizationStep(MovementResult movementResult)
    {
        movementResult.setActualStep(false);
        long smallestTimestamp = findMinimalTokenTime();
        long secondSmallestTimestamp = findNextMinimalTimestamp(smallestTimestamp);
        if (getGenerationDescription().isUsingSynchronizationOnResources())
        {
            long minimalResourceTime = TimeDrivenLoggingSingleton.timeDrivenInstance().getNearestResourceTime(getNode());
            if (secondSmallestTimestamp < minimalResourceTime)
            {
                secondSmallestTimestamp = minimalResourceTime;
            }
        }
        for (TimeDrivenPlace place : getInputPlaces())
        {
            if (place.getLowestTimestamp() == smallestTimestamp)
            {
                TimeDrivenToken token = place.consumeToken();
                TimeDrivenToken copy = token.copyTokenWithNewTimestamp(secondSmallestTimestamp);
                place.addToken(copy);
            }
        }
    }


    private boolean checkTimeOfTokens()
    {
        long time = -1;
        for (TimeDrivenPlace place : getInputPlaces())
        {
            if (time == -1)
            {
                time = place.getLowestTimestamp();
            }
            else
            {
                if (time != place.getLowestTimestamp())
                {
                    return false;
                }
            }
        }
        return true;
    }


    public long findMinimalTokenTime()
    {
        long minimalTimestamp = Long.MAX_VALUE;
        for (TimeDrivenPlace place : getInputPlaces())
        {
            if (minimalTimestamp > place.getLowestTimestamp())
            {
                minimalTimestamp = place.getLowestTimestamp();
            }
        }
        return minimalTimestamp;
    }

    @Override
    public int compareTo(TimeDrivenTransition o)
    {
        long instanceMinimalTimestamp = findMinimalTokenTime();
        long parameterMinimalTimestamp = o.findMinimalTokenTime();
        if (instanceMinimalTimestamp == parameterMinimalTimestamp)
        {
            return 0;
        }
        if (instanceMinimalTimestamp < parameterMinimalTimestamp)
        {
            return -1;
        }
        return 1;
    }

    private void actuallyMove(XTrace trace, MovementResult<TimeDrivenToken> movementResult)
    {
        moveTokensFromPrecedingPlaces(trace, movementResult);
    }

    private int getDistortionType()
    {
        return random.nextInt(3);
    }

    private void moveTokensFromPrecedingPlaces(XTrace trace, MovementResult<TimeDrivenToken> movementResult)
    {
        long timestamp = consumeTokens();
        startTransition(trace, movementResult, timestamp);
    }

    private long consumeTokens()//TODO а нельзя ли как-то хитро заюзать его в базовом классе?
    {
        long timestamp = 0;
        for (TimeDrivenPlace place : getInputPlaces())
        {
            TimeDrivenToken consumedToken = place.consumeToken();
            timestamp = consumedToken.getTimestamp();
        }
        return timestamp;
    }

    private void startTransition(XTrace trace, MovementResult<TimeDrivenToken> movementResult, long timeStamp)
    {
        long timeDeviation = (long) (random.nextDouble() * (maxTimeDeviation + 1));
        if (random.nextBoolean())
        {
            timeDeviation = -timeDeviation;
        }
        TimeDrivenToken producedToken;
        if (getGenerationDescription().isUsingResources())
        {
            Resource usedResource = TimeDrivenLoggingSingleton.timeDrivenInstance().logStartEventWithResource(trace, getNode(), timeStamp);
            long finishTime = timeStamp + (executionTime + timeDeviation) * 1000;
            if (getGenerationDescription().isUsingSynchronizationOnResources())
            {
                usedResource.setTime(finishTime);
                setResourceTime(usedResource, finishTime);
            }
            producedToken = new TimeDrivenToken(this, usedResource, finishTime);
        }
        else
        {
            if (getGenerationDescription().isSeparatingStartAndFinish())
            {
                TimeDrivenLoggingSingleton.timeDrivenInstance().log(trace, getNode(), timeStamp, false);
            }
            producedToken = new TimeDrivenToken(this, timeStamp + (executionTime + timeDeviation) * 1000);
        }
        movementResult.addProducedExtraToken(producedToken);
    }

    private void setResourceTime(Resource resource, long finishTime)
    {
        if (getGenerationDescription().isUsingSynchronizationOnResources())
        {
            long minDelay = resource.getMinDelayBetweenActions();
            long maxDelay = resource.getMaxDelayBetweenActions();
            long difference = maxDelay - minDelay;
            long actualDelay = random.nextLong() % (difference + 1);
            finishTime += minDelay + actualDelay;
        }
        resource.setTime(finishTime);
    }

    private Pair<Object, Resource> registerNoiseTransition(XTrace trace, long timestamp, MovementResult<TimeDrivenToken> movementResult)
    {
        TimeDrivenLoggingSingleton loggingSingleton = TimeDrivenLoggingSingleton.timeDrivenInstance();
        List<NoiseEvent> noiseEvents = getNoiseEventsBasedOnSettings();
        List<NoiseEvent> noiseEventList = new LinkedList<NoiseEvent>(noiseEvents);
        while (noiseEventList.size() > 0)
        {
            System.out.println("Number of noise events: " + noiseEventList.size());
            NoiseEvent noiseEvent = noiseEventList.remove(random.nextInt(noiseEventList.size()));
            if (getGenerationDescription().isUsingSynchronizationOnResources() && !loggingSingleton.areResourcesAvailable(noiseEvent.getActivity(), timestamp))
            {
                continue;
            }
            System.out.println("Added " + noiseEvent.getActivity() + " as a noise event"); //TODO delete?
            Resource usedResource = null;
            if (getGenerationDescription().isUsingResources())
            {
                usedResource = loggingSingleton.logStartEventWithResource(trace, noiseEvent.getActivity(), timestamp);
            }
            else
            {
                if (getGenerationDescription().isSeparatingStartAndFinish())
                {
                    loggingSingleton.log(trace, noiseEvent, timestamp, false);
                }
            }
            long timeDeviation = (long) (random.nextDouble() * (noiseEvent.getMaxTimeDeviation() + 1));
            if (random.nextBoolean())
            {
                timeDeviation = -timeDeviation;
            }
            long totalExecutionTime = (noiseEvent.getExecutionTime() + timeDeviation) * 1000;
            long finishTime = timestamp + totalExecutionTime;
            if (usedResource != null && getGenerationDescription().isUsingSynchronizationOnResources())
            {
                usedResource.setTime(finishTime);
            }
            NoiseToken noiseToken = new NoiseToken(noiseEvent, usedResource, finishTime);
            movementResult.addProducedExtraToken(noiseToken);
            return new Pair<Object, Resource>(noiseEvent, usedResource);
        }
        return null;
    }

    private List<NoiseEvent> getNoiseEventsBasedOnSettings()
    {
        List<NoiseEvent> noiseEvents = new ArrayList<NoiseEvent>();
        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = getGenerationDescription().getNoiseDescription();
        if (noiseDescription.isUsingInternalTransitions())
        {
            noiseEvents.addAll(noiseDescription.getExistingNoiseEvents());
        }
        if (noiseDescription.isUsingExternalTransitions())
        {
            noiseEvents.addAll(noiseDescription.getArtificialNoiseEvents());
        }
        return noiseEvents;
    }

    private boolean shouldDistortEvent()
    {
        if (getGenerationDescription().isUsingNoise())
        {
            TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = getGenerationDescription().getNoiseDescription();
            if (noiseDescription.getNoisedLevel() >= random.nextInt(org.processmining.models.descriptions.GenerationDescriptionWithNoise.MAX_NOISE_LEVEL + 1))  //use noise transitions
            {
                if (noiseDescription.isUsingInternalTransitions() || noiseDescription.isUsingExternalTransitions())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private long findNextMinimalTimestamp(long timestamp)
    {
        long nextMinimalTimestamp = Long.MAX_VALUE;
        for (TimeDrivenPlace place : getInputPlaces())
        {
            long currentTimestamp = place.getLowestTimestamp();
            if (currentTimestamp < nextMinimalTimestamp && currentTimestamp > timestamp)
            {
                nextMinimalTimestamp = currentTimestamp;
            }
        }
        if (nextMinimalTimestamp == Long.MAX_VALUE)
        {
            nextMinimalTimestamp = timestamp;
        }
        return nextMinimalTimestamp;
    }

    public MovementResult moveInternalToken(XTrace trace, TimeDrivenToken token)
    {
        MovementResult<TimeDrivenToken> movementResult = new MovementResult<TimeDrivenToken>();
        movementResult.addConsumedExtraToken(token);
        long maxTimeStamp = token.getTimestamp();
        if (getGenerationDescription().isUsingResources())
        {
            completeTransition(trace, token.getResource(), maxTimeStamp);
        }
        else
        {
            completeTransition(trace, maxTimeStamp);
        }
        return movementResult;
    }

    private void completeTransition(XTrace trace, long maxTimeStamp)
    {
        TimeDrivenLoggingSingleton.timeDrivenInstance().log(trace, getNode(), maxTimeStamp, true);
        addTokensToOutputPlaces(maxTimeStamp);
    }

    private void completeTransition(XTrace trace, Resource resource, long maxTimeStamp)
    {
        TimeDrivenLoggingSingleton.timeDrivenInstance().logCompleteEventWithResource(trace, getNode(), resource, maxTimeStamp);
        addTokensToOutputPlaces(maxTimeStamp);
    }

    private void addTokensToOutputPlaces(long maxTimeStamp)
    {
        TimeDrivenGenerationDescription description = getGenerationDescription();
        int possibleTimeVariation = description.getMaximumIntervalBetweenActions() - description.getMinimumIntervalBetweenActions();
        for (TimeDrivenPlace place : getOutputPlaces())
        {
            int timeBetweenActions = (description.getMinimumIntervalBetweenActions() + random.nextInt(possibleTimeVariation + 1));
            TimeDrivenToken token = new TimeDrivenToken(place, maxTimeStamp + timeBetweenActions * 1000);
            place.addToken(token);
        }
    }

    @Override
    public TimeDrivenGenerationDescription getGenerationDescription()
    {
        return (TimeDrivenGenerationDescription) super.getGenerationDescription();
    }

    public static class TimeDrivenTransitionBuilder
    {
        private final org.processmining.models.graphbased.directed.petrinet.elements.Transition transition;
        private final TimeDrivenGenerationDescription description;
        private final List<TimeDrivenPlace> inputPlaces = new ArrayList<TimeDrivenPlace>();
        private final List<TimeDrivenPlace> outputPlaces = new ArrayList<TimeDrivenPlace>();

        public TimeDrivenTransitionBuilder(org.processmining.models.graphbased.directed.petrinet.elements.Transition transition, TimeDrivenGenerationDescription description)
        {
            if (transition == null)
            {
                throw new NullPointerException("Transition cannot be equal to null");
            }

            if (description == null)
            {
                throw new NullPointerException("Generation description cannot be equal to null");
            }

            this.transition = transition;
            this.description = description;
        }

        public TimeDrivenTransitionBuilder inputPlace(TimeDrivenPlace inputPlace)
        {
            if (inputPlace == null)
            {
                throw new NullPointerException("Input place cannot be null");
            }

            inputPlaces.add(inputPlace);

            return this;
        }

        public TimeDrivenTransitionBuilder outputPlace(TimeDrivenPlace outputPlace)
        {
            if (outputPlace == null)
            {
                throw new NullPointerException("Place cannot be equal to null");
            }

            outputPlaces.add(outputPlace);

            return this;
        }

        public TimeDrivenTransition build()
        {
            TimeDrivenPlace[] inputPlacesArray = inputPlaces.toArray(new TimeDrivenPlace[inputPlaces.size()]);
            TimeDrivenPlace[] outputPlacesArray = outputPlaces.toArray(new TimeDrivenPlace[outputPlaces.size()]);

            return new TimeDrivenTransition(transition, description, inputPlacesArray, outputPlacesArray);
        }
    }
}
