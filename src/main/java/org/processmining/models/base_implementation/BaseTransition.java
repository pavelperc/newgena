package org.processmining.models.base_implementation;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.GenerationDescription;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.AbstractPetriNode;
import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.abstract_net_representation.Transition;
import org.processmining.utils.LoggingSingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
public class BaseTransition extends Transition<Place<Token>>
{
    protected BaseTransition(org.processmining.models.graphbased.directed.petrinet.elements.Transition transition, GenerationDescription generationDescription, Place<Token>[] inputPlaces, Place<Token>[] outputPlaces)
    {
        super(transition, generationDescription, inputPlaces, outputPlaces);
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        consumeTokens();
        LoggingSingleton.log(trace, getNode());
        produceTokens();
        return new MovementResult();
    }

    protected void produceTokens()
    {
        for (AbstractPetriNode node : getOutputPlaces())
        {
            Place place = (Place) node;
            Token token = new Token();
            place.addToken(token);
        }
    }

    protected void consumeTokens()
    {
        for (Place place : getInputPlaces())
        {
            place.consumeToken();
        }
    }

    public static class BaseTransitionBuilder
    {
        private final org.processmining.models.graphbased.directed.petrinet.elements.Transition transition;
        private final GenerationDescription description;
        private final List<Place> inputPlaces = new ArrayList<Place>();
        private final List<Place> outputPlaces = new ArrayList<Place>();

        public BaseTransitionBuilder(org.processmining.models.graphbased.directed.petrinet.elements.Transition transition, GenerationDescription/*TODO ужен свой тип?*/ description)
        {
            if (transition == null)
            {
                throw new IllegalArgumentException("Transition cannot be equal to null");
            }

            if (description == null)
            {
                throw new IllegalArgumentException("Generation description cannot be equal to null");
            }

            this.transition = transition;
            this.description = description;
        }

        public BaseTransitionBuilder inputPlace(Place<Token> inputPlace)
        {
            if (inputPlace == null)
            {
                throw new NullPointerException("Place cannot be equal to null");
            }

            inputPlaces.add(inputPlace);

            return this;
        }

        public BaseTransitionBuilder outputPlace(Place<Token> outputPlace)
        {
            if (outputPlace == null)
            {
                throw new NullPointerException("Output place cannot be null");
            }

            outputPlaces.add(outputPlace);

            return this;
        }

        public BaseTransition build()
        {
            @SuppressWarnings("unchecked")
            Place<Token>[] inputPlacesArray = inputPlaces.toArray(new Place[inputPlaces.size()]);

            @SuppressWarnings("unchecked")
            Place<Token>[] outputPlacesArray = outputPlaces.toArray(new Place[outputPlaces.size()]);

            return new BaseTransition(transition, description, inputPlacesArray, outputPlacesArray);
        }
    }
}
