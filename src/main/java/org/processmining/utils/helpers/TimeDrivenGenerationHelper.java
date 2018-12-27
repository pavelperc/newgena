package org.processmining.utils.helpers;

import org.processmining.models.Movable;
import org.processmining.models.abstract_net_representation.Transition;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import org.processmining.models.time_driven_behavior.TimeDrivenPlace;
import org.processmining.models.time_driven_behavior.TimeDrivenToken;
import org.processmining.models.time_driven_behavior.TimeDrivenTransition;

import java.util.*;

/**
 * Created by Ivan Shugurov on 24.10.2014.
 */
public class TimeDrivenGenerationHelper extends PetriNetGenerationHelper<TimeDrivenPlace, TimeDrivenTransition, TimeDrivenToken>
{

    private final long generationStart;

    public TimeDrivenGenerationHelper(Collection<TimeDrivenPlace> initialMarking, Collection<TimeDrivenPlace> finalMarking, Collection<TimeDrivenPlace> allPlaces, Collection<TimeDrivenTransition> allTransitions, TimeDrivenGenerationDescription description)
    {
        super(initialMarking, finalMarking, allTransitions, allPlaces, description);
        Calendar startCalendar = description.getGenerationStart();
        generationStart = startCalendar.getTimeInMillis();
    }

    public static TimeDrivenGenerationHelper createInstance(Petrinet petrinet, Marking initialMarking, Marking finalMarking, TimeDrivenGenerationDescription description)
    {
        checkConstructorParameters(petrinet, initialMarking, finalMarking, description);

        List<TimeDrivenPlace> initialPlaces = new ArrayList<TimeDrivenPlace>();
        List<TimeDrivenPlace> finalPlaces = new ArrayList<TimeDrivenPlace>();

        Map<NodeID, TimeDrivenPlace> nodesToPlaces = new HashMap<NodeID, TimeDrivenPlace>();

        List<TimeDrivenPlace> allPlaces = new ArrayList<TimeDrivenPlace>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Place place : petrinet.getPlaces())
        {
            TimeDrivenPlace loggablePlace = new TimeDrivenPlace(place, description);
            allPlaces.add(loggablePlace);

            if (initialPlaces.size() != initialMarking.size() && initialMarking.contains(place))
            {
                initialPlaces.add(loggablePlace);
            }
            else
            {
                if (finalPlaces.size() != finalMarking.size() && finalMarking.contains(place))
                {
                    finalPlaces.add(loggablePlace);
                }
            }

            nodesToPlaces.put(place.getId(), loggablePlace);
        }

        List<TimeDrivenTransition> allTransitions = new ArrayList<TimeDrivenTransition>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition transition : petrinet.getTransitions())
        {
            TimeDrivenTransition.TimeDrivenTransitionBuilder transitionBuilder = new TimeDrivenTransition.TimeDrivenTransitionBuilder(transition, description);

            //gets out edges
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = petrinet.getOutEdges(transition);
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outEdges)
            {
                NodeID id = edge.getTarget().getId();
                TimeDrivenPlace outputPlace = nodesToPlaces.get(id);
                transitionBuilder.outputPlace(outputPlace);
            }

            //get in edges
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = petrinet.getInEdges(transition);
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inEdges)
            {
                NodeID id = edge.getSource().getId();
                TimeDrivenPlace inputPlace = nodesToPlaces.get(id);
                transitionBuilder.inputPlace(inputPlace);
            }

            TimeDrivenTransition simpleTransition = transitionBuilder.build();
            allTransitions.add(simpleTransition);
        }

        return new TimeDrivenGenerationHelper(initialPlaces, finalPlaces, allPlaces, allTransitions, description);
    }

    private static void checkConstructorParameters(Petrinet petrinet, Marking initialMarking, Marking finalMarking, TimeDrivenGenerationDescription description)
    {
        if (petrinet == null)
        {
            throw new IllegalArgumentException("Petrinet cannot be null");
        }

        if (initialMarking == null)
        {
            throw new IllegalArgumentException("Initial marking cannot be null");
        }

        if (finalMarking == null)
        {
            throw new IllegalArgumentException("Final marking cannot be null");
        }

        if (description == null)
        {
            throw new IllegalArgumentException("Generation description cannot be null");
        }
    }


    @Override
    public TimeDrivenGenerationDescription getGenerationDescription()
    {
        return (TimeDrivenGenerationDescription) super.getGenerationDescription();
    }

    @Override
    protected void putInitialToken(TimeDrivenPlace place)
    {
        TimeDrivenToken token = new TimeDrivenToken(place, generationStart);
        place.addToken(token);
    }

    @Override
    public void moveToInitialState()
    {
        super.moveToInitialState();

        TimeDrivenGenerationDescription description = getGenerationDescription();
        if (description.isUsingSynchronizationOnResources())
        {
            for (Resource resource : description.getSimplifiedResources())
            {
                resource.setTime(0);
                resource.setIdle(true);
            }
            for (Group group : description.getResourceGroups())
            {
                for (Resource resource : group.getResources())
                {
                    resource.setTime(0);
                    resource.setIdle(true);
                }
            }
        }
    }

    @Override
    protected boolean tokensOnlyInFinalMarking()
    {
        if (getExtraMovables().isEmpty())
        {
            return super.tokensOnlyInFinalMarking();
        }
        else
        {
            return false;
        }
    }

    @Override
    public Movable chooseNextMovable()
    {
        TreeMap<Long, List<Transition>> enabledTransitions = new TreeMap<Long, List<Transition>>();
        for (TimeDrivenTransition transition : getAllModelMovables())
        {
            if (transition.checkAvailability())
            {
                long timestamp = transition.findMinimalTokenTime();
                if (enabledTransitions.containsKey(timestamp))
                {
                    List<Transition> transitionsWithGivenTimestamp = enabledTransitions.get(timestamp);
                    transitionsWithGivenTimestamp.add(transition);
                }
                else
                {
                    List<Transition> transitions = new ArrayList<Transition>();
                    transitions.add(transition);
                    enabledTransitions.put(timestamp, transitions);
                }

            }
        }
        Movable movable;
        if (enabledTransitions.isEmpty() && getExtraMovables().isEmpty())
        {
            return null;
        }
        else
        {
            if (enabledTransitions.isEmpty())
            {
                TreeMap<Long, List<TimeDrivenToken>> tokensMap = sortExtraMovables();
                Map.Entry<Long, List<TimeDrivenToken>> entryWithSmallestTimestamp = tokensMap.firstEntry();
                List<TimeDrivenToken> tokens = entryWithSmallestTimestamp.getValue();
                movable = pickRandomMovable(tokens);
            }
            else
            {
                if (getExtraMovables().isEmpty())
                {
                    Map.Entry<Long, List<Transition>> entry = enabledTransitions.firstEntry();
                    List<Transition> movables = entry.getValue();
                    movable = pickRandomMovable(movables);
                }
                else
                {
                    TreeMap<Long, List<TimeDrivenToken>> extraMovablesMap = sortExtraMovables();

                    long earliestTransitionTime = enabledTransitions.firstKey();
                    long earliestExtraMovableTime = extraMovablesMap.firstKey();

                    if (earliestTransitionTime < earliestExtraMovableTime)
                    {
                        Map.Entry<Long, List<Transition>> entry = enabledTransitions.firstEntry();
                        List<Transition> movables = entry.getValue();
                        movable = pickRandomMovable(movables);
                    }
                    else
                    {
                        if (earliestTransitionTime > earliestExtraMovableTime)
                        {
                            List<TimeDrivenToken> earliestExtraMovables = extraMovablesMap.get(earliestExtraMovableTime);
                            movable = pickRandomMovable(earliestExtraMovables);
                        }
                        else
                        {
                            boolean useTransition = random.nextBoolean();
                            if (useTransition)
                            {
                                Map.Entry<Long, List<Transition>> entry = enabledTransitions.firstEntry();
                                List<Transition> movables = entry.getValue();
                                movable = pickRandomMovable(movables);
                            }
                            else
                            {
                                List<TimeDrivenToken> tokensWithSmallestTimestamp = extraMovablesMap.get(earliestTransitionTime);
                                movable = pickRandomMovable(tokensWithSmallestTimestamp);
                            }
                        }
                    }

                }
            }
        }
        return movable;
    }

    private TreeMap<Long, List<TimeDrivenToken>> sortExtraMovables()
    {
        TreeMap<Long, List<TimeDrivenToken>> tokensMap = new TreeMap<Long, List<TimeDrivenToken>>();
        for (TimeDrivenToken token : getExtraMovables())
        {
            long time = token.getTimestamp();
            if (tokensMap.containsKey(time))
            {
                List<TimeDrivenToken> tokensWithCurrentTimestamp = tokensMap.get(time);
                tokensWithCurrentTimestamp.add(token);
            }
            else
            {
                List<TimeDrivenToken> tokensWithCurrentTimestamp = new ArrayList<TimeDrivenToken>();
                tokensWithCurrentTimestamp.add(token);
                tokensMap.put(time, tokensWithCurrentTimestamp);
            }
        }
        return tokensMap;
    }
}
