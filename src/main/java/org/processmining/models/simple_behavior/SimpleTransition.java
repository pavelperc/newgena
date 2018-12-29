package org.processmining.models.simple_behavior;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_implementation.BaseTransition;
import org.processmining.models.descriptions.GenerationDescriptionWithNoise;
import org.processmining.models.descriptions.SimpleGenerationDescription;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.utils.LoggingSingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Shugurov on 30.10.2014.
 */
public class SimpleTransition extends BaseTransition
{
    protected SimpleTransition(Transition transition, SimpleGenerationDescription generationDescription, Place<Token>[] inputPlaces, Place<Token>[] outputPlaces)
    {
        super(transition, generationDescription, inputPlaces, outputPlaces);
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        consumeTokens();

        if (shouldDistortEvent())
        {
            SimpleGenerationDescription description = getGenerationDescription();
            GenerationDescriptionWithNoise.NoiseDescription noiseDescription = description.getNoiseDescription();

            if (noiseDescription.isSkippingTransitions())
            {
                if (noiseDescription.isUsingInternalTransitions() || noiseDescription.isUsingExternalTransitions())
                {
                    boolean skipEvent = random.nextBoolean();

                    if (skipEvent)
                    {
                        //ignore this case in order to skip the event
                    }
                    else
                    {
                        logNoiseAndTransition(trace);
                    }
                }
                else
                {
                    //ignore this case in order to skip the event
                }
            }
            else
            {
                logNoiseAndTransition(trace);
            }
        }
        else
        {
            logTransition(trace);
        }

        produceTokens();
        return new MovementResult();
    }

    protected void logNoiseAndTransition(XTrace trace)
    {
        boolean noiseFirst = random.nextBoolean();

        if (noiseFirst)
        {
            logNoiseEvent(trace);
            logTransition(trace);
        }
        else
        {
            LoggingSingleton.log(trace, getNode());
            logTransition(trace);
        }
    }

    private void logTransition(XTrace trace)
    {
        Transition realTransition = (Transition) getNode();
        if (!realTransition.isInvisible())
        {
            LoggingSingleton.log(trace, getNode());
        }
    }

    protected void logNoiseEvent(XTrace trace)
    {
        List<Object> noiseEvents = getNoiseEventsBasedOnSettings();
        if (!noiseEvents.isEmpty())
        {
            int index = random.nextInt(noiseEvents.size());
            Object event = noiseEvents.get(index);
            LoggingSingleton.log(trace, event);
        }
    }

    private List<Object> getNoiseEventsBasedOnSettings()
    {
        List<Object> noiseEvents = new ArrayList<Object>();
        GenerationDescriptionWithNoise.NoiseDescription noiseDescription = getGenerationDescription().getNoiseDescription();
        if (noiseDescription.isUsingInternalTransitions())
        {
            noiseEvents.addAll(noiseDescription.getInternalTransitions());   //TODO почему здесь и в версии с временами по-разному работает этот метод?
        }
        if (noiseDescription.isUsingExternalTransitions())
        {
            noiseEvents.addAll(noiseDescription.getArtificialNoiseEvents());
        }
        return noiseEvents;
    }

    private boolean shouldDistortEvent()
    {
        GenerationDescriptionWithNoise description = getGenerationDescription();
        if (description.isUsingNoise())
        {
            GenerationDescriptionWithNoise.NoiseDescription noiseDescription = description.getNoiseDescription();
            if (noiseDescription.getNoisedLevel() >= random.nextInt(GenerationDescriptionWithNoise.NoiseDescription.MAX_NOISE_LEVEL + 1))  //use noise transitions
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public SimpleGenerationDescription getGenerationDescription()
    {
        return (SimpleGenerationDescription) super.getGenerationDescription();
    }

    public static class SimpleTransitionBuilder
    {
        private final Transition transition;
        private final SimpleGenerationDescription description;
        private final List<Place<Token>> inputPlaces = new ArrayList<Place<Token>>();
        private final List<Place<Token>> outputPlaces = new ArrayList<Place<Token>>();


        public SimpleTransitionBuilder(Transition transition, SimpleGenerationDescription description)
        {
            if (transition == null)
            {
                throw new NullPointerException("Transition cannot be equal to null");
            }

            if (description == null)
            {
                throw new NullPointerException("Generation description cannot be null");
            }

            this.transition = transition;
            this.description = description;
        }

        public SimpleTransitionBuilder inputPlace(Place<Token> inputPlace)
        {
            if (inputPlace == null)
            {
                throw new NullPointerException("Place cannot be equal to null");
            }

            inputPlaces.add(inputPlace);

            return this;
        }

        public SimpleTransitionBuilder outputPlace(Place<Token> outputPlace)
        {
            if (outputPlace == null)
            {
                throw new NullPointerException("Place cannot be equal to null");
            }

            outputPlaces.add(outputPlace);

            return this;
        }

        public SimpleTransition build()
        {
            @SuppressWarnings("unchecked")
            Place<Token>[] inputPlacesArray = inputPlaces.toArray(new Place[inputPlaces.size()]);
            @SuppressWarnings("unchecked")
            Place<Token>[] outputPlacesArray = outputPlaces.toArray(new Place[outputPlaces.size()]);

            return new SimpleTransition(transition, description, inputPlacesArray, outputPlacesArray);
        }
    }
}
