package org.processmining.utils.helpers;

import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.descriptions.SimpleGenerationDescription;
import org.processmining.models.simple_behavior.SimpleTransition;

import java.util.*;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class SimpleGenerationHelper extends PetriNetGenerationHelper<Place<Token>, SimpleTransition, Token>
{
    public SimpleGenerationHelper(Collection<Place<Token>> initialMarking, Collection<Place<Token>> finalMarking, Collection<SimpleTransition> allTransitions, Collection<Place<Token>> allPlaces, SimpleGenerationDescription description)
    {
        super(initialMarking, finalMarking, allTransitions, allPlaces, description);
    }

    public static SimpleGenerationHelper createHelper(Petrinet petrinet, Marking initialMarking, Marking finalMarking, SimpleGenerationDescription description)
    {
        checkConstructorParameters(petrinet, initialMarking, finalMarking, description);

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

        List<SimpleTransition> allTransitions = new ArrayList<SimpleTransition>();
        for (org.processmining.models.graphbased.directed.petrinet.elements.Transition transition : petrinet.getTransitions())
        {
            SimpleTransition.SimpleTransitionBuilder transitionBuilder = new SimpleTransition.SimpleTransitionBuilder(transition, description);

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

            SimpleTransition simpleTransition = transitionBuilder.build();
            allTransitions.add(simpleTransition);
        }
        return new SimpleGenerationHelper(initialPlaces, finalPlaces, allTransitions, allPlaces, description);
    }

    private static void checkConstructorParameters(Petrinet petrinet, Marking initialMarking, Marking finalMarking, SimpleGenerationDescription description)
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
    protected void putInitialToken(Place<Token> place)
    {
        Token token = new Token();
        place.addToken(token);
    }
}
