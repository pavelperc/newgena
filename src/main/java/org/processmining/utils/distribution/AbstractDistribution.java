package org.processmining.utils.distribution;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import java.util.Date;

/**
 * Created by Ivan on 11.08.2015.
 */
public abstract class AbstractDistribution implements ProbabilityDistribution
{
    private static final RandomEngine cernEngine = new MersenneTwister(new Date());
    private AbstractContinousDistribution distribution;

    public AbstractDistribution()
    {

    }

    protected static RandomEngine getGenerator()
    {
        return cernEngine;
    }

    public void checkMinMax(long min, long max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Min value cannot be bigger than max value");
        }
    }

    public void checkMinMax(double min, double max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Min value cannot be bigger than max value");
        }
    }


    @Override
    public long nextLong()
    {
        return nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    public long nextLong(long min, long max)
    {
        if (distribution == null)
        {
            distribution = initDistribution();
        }

        checkMinMax(min, max);

        long generatedValue;

        do
        {
            generatedValue = (long)distribution.nextDouble();
        }
        while (generatedValue < min || generatedValue > max);

        return generatedValue;
    }

    @Override
    public double nextDouble()
    {
        if (distribution == null)
        {
            distribution = initDistribution();
        }

        return distribution.nextDouble();
    }

    @Override
    public double nextDouble(double min, double max)
    {
        if (distribution == null)
        {
            distribution = initDistribution();
        }

        checkMinMax(min, max);

        double value;

        do
        {
            value = distribution.nextDouble();

        } while (value < min || value > max);

        return value;
    }

    public AbstractContinousDistribution getDistribution()
    {
        return distribution;
    }

    protected abstract AbstractContinousDistribution initDistribution();

}
