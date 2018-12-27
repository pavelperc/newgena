package org.processmining.utils.helpers;

import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities;
import org.processmining.models.Movable;
import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_implementation.BaseTransition;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.*;

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
public class StaticPrioritiesGenerationHelper extends PetriNetGenerationHelper<Place<Token>, BaseTransition, Token>
{
    private TreeMap<Integer, List<BaseTransition>> priorities;

    public StaticPrioritiesGenerationHelper(Collection<Place<Token>> initialMarking, Collection<Place<Token>> finalMarking, Collection<BaseTransition> allTransitions, Collection<Place<Token>> allPlaces, GenerationDescriptionWithStaticPriorities description, TreeMap<Integer, List<BaseTransition>> priorities)
    {
        super(initialMarking, finalMarking, allTransitions, allPlaces, description);
        this.priorities = priorities;
    }

    public static StaticPrioritiesGenerationHelper createStaticPrioritiesGenerationHelper(Petrinet petrinet, Marking initialMarking, Marking finalMarking, GenerationDescriptionWithStaticPriorities description)
    {
        checkConstructorParameters(petrinet, initialMarking, finalMarking, description);

        TreeMap<Integer, List<BaseTransition>> modifiedPriorities = new TreeMap<Integer, List<BaseTransition>>();

        List<Place<Token>> initialPlaces = new ArrayList<Place<Token>>();
        List<Place<Token>> finalPlaces = new ArrayList<Place<Token>>();

        Map<NodeID, Place<Token>> nodesToPlaces = new HashMap<NodeID, Place<Token>>();

        List<Place<Token>> allPlaces = new ArrayList<Place<Token>>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Place place : petrinet.getPlaces())
        {
            Place<Token> loggablePlace = new Place<Token>(place, description);
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

        List<BaseTransition> allTransitions = new ArrayList<BaseTransition>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition transition : petrinet.getTransitions())
        {
            BaseTransition.BaseTransitionBuilder transitionBuilder = new BaseTransition.BaseTransitionBuilder(transition, description);

            //gets out edges
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = petrinet.getOutEdges(transition);
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : outEdges)
            {
                NodeID id = edge.getTarget().getId();
                Place<Token> outputPlace = nodesToPlaces.get(id);
                transitionBuilder.outputPlace(outputPlace);
            }

            //get in edges
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = petrinet.getInEdges(transition);
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inEdges)
            {
                NodeID id = edge.getSource().getId();
                Place<Token> inputPlace = nodesToPlaces.get(id);
                transitionBuilder.inputPlace(inputPlace);
            }

            BaseTransition loggableTransition = transitionBuilder.build();
            allTransitions.add(loggableTransition);

            int priority = description.getPriority(transition);
            if (modifiedPriorities.containsKey(priority))
            {
                List<BaseTransition> transitions = modifiedPriorities.get(priority);
                transitions.add(loggableTransition);
            }
            else
            {
                List<BaseTransition> transitions = new ArrayList<BaseTransition>();
                transitions.add(loggableTransition);
                modifiedPriorities.put(priority, transitions);
            }
        }

        return new StaticPrioritiesGenerationHelper(initialPlaces, finalPlaces, allTransitions, allPlaces, description, modifiedPriorities);
    }

    private static void checkConstructorParameters(Petrinet petrinet, Marking initialMarking, Marking finalMarking, GenerationDescriptionWithStaticPriorities description)
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
    public Movable chooseNextMovable()
    {
        for (int priority : priorities.descendingKeySet())
        {
            List<BaseTransition> transitions = priorities.get(priority);
            List<BaseTransition> enabledTransitions = new ArrayList<BaseTransition>();
            for (BaseTransition transition : transitions)
            {
                if (transition.checkAvailability())
                {
                    enabledTransitions.add(transition);
                }
            }
            if (!enabledTransitions.isEmpty())
            {
                return pickRandomMovable(enabledTransitions);
            }
        }
        return null;
    }

    @Override
    protected void putInitialToken(Place<Token> place)
    {
        Token token = new Token();
        place.addToken(token);
    }
}
