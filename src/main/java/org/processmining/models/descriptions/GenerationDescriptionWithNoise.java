package org.processmining.models.descriptions;

import org.processmining.models.time_driven_behavior.NoiseEvent;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Shugurov on 29.12.2014.
 */
public abstract class GenerationDescriptionWithNoise extends BaseGenerationDescription//TODo а как я его использую, если он абстрактный?
{
    private final NoiseDescription noiseDescription;
    private boolean usingNoise = true;
    private boolean removingUnfinishedTraces = true;
    private boolean removingEmptyTraces = true;

    public GenerationDescriptionWithNoise()
    {
        this.noiseDescription = new NoiseDescription(this);
    }

    protected GenerationDescriptionWithNoise(NoiseDescription description)
    {
        this.noiseDescription = description;
    }

    public boolean isUsingNoise()
    {
        return usingNoise;
    }

    public void setUsingNoise(boolean usingNoise)
    {
        this.usingNoise = usingNoise;
    }

    public NoiseDescription getNoiseDescription()
    {
        return noiseDescription;
    }

    @Override
    public boolean isRemovingEmptyTraces()
    {
        return removingEmptyTraces;
    }

    public void setRemovingEmptyTraces(boolean removingEmptyTraces)
    {
        this.removingEmptyTraces = removingEmptyTraces;
    }

    @Override
    public boolean isRemovingUnfinishedTraces()
    {
        return removingUnfinishedTraces;
    }

    public void setRemovingUnfinishedTraces(boolean removingUnfinishedTraces)
    {
        this.removingUnfinishedTraces = removingUnfinishedTraces;
    }

    public static class NoiseDescription
    {
        public static final int MIN_NOISE_LEVEL = 1;
        public static final int MAX_NOISE_LEVEL = 100;
        private final GenerationDescriptionWithNoise description;
        private int noisedLevel = 5;
        private boolean usingExternalTransitions = true;
        private boolean usingInternalTransitions = true;
        private boolean isSkippingTransitions = true;
        private List<Transition> internalTransitions = new ArrayList<Transition>();
        private List<NoiseEvent> existingNoiseEvents = new ArrayList<NoiseEvent>();
        private List<NoiseEvent> artificialNoiseEvents = new ArrayList<NoiseEvent>();

        protected NoiseDescription(GenerationDescriptionWithNoise description)  //TODO долен быть private?
        {
            this.description = description;
        }

        protected GenerationDescriptionWithNoise getGenerationDescription()
        {
            return description;
        }

        public int getNoisedLevel()
        {
            return noisedLevel;
        }

        public void setNoisedLevel(int noisedLevel)
        {
            if (noisedLevel < MIN_NOISE_LEVEL || noisedLevel > MAX_NOISE_LEVEL)
            {
                throw new IllegalArgumentException("Precondition violated in NoiseDescription. Unaccepted noise level");
            }
            this.noisedLevel = noisedLevel;
        }

        public boolean isUsingInternalTransitions()
        {
            return description.isUsingNoise() && usingInternalTransitions;
        }

        public void setUsingInternalTransitions(boolean usingInternalTransitions)
        {
            this.usingInternalTransitions = usingInternalTransitions;
        }

        public boolean isUsingExternalTransitions()
        {
            return description.isUsingNoise() && usingExternalTransitions;
        }

        public void setUsingExternalTransitions(boolean usingExternalTransitions)
        {
            this.usingExternalTransitions = usingExternalTransitions;
        }

        public List<NoiseEvent> getArtificialNoiseEvents()
        {
            return artificialNoiseEvents;
        }

        public List<Transition> getInternalTransitions()
        {
            return internalTransitions;
        }

        public void setInternalTransitions(List<Transition> internalTransitions)
        {
            this.internalTransitions = internalTransitions;
        }

        public List<NoiseEvent> getExistingNoiseEvents()
        {
            return existingNoiseEvents;
        }    //TODO мне не нравится название(

        public void setExistingNoiseEvents(List<NoiseEvent> existingNoiseEvents)
        {
            this.existingNoiseEvents = existingNoiseEvents;
        }

        public boolean isSkippingTransitions()
        {
            return isSkippingTransitions;
        }

        public void setSkippingTransitions(boolean skippingTransitions)
        {
            isSkippingTransitions = skippingTransitions;
        }

    }
}
