package org.processmining.utils.distribution;

/**
 * Created by Ivan on 27.11.2015.
 */
public class ConfiguredDoubleDistribution implements ConfiguredDistribution
{
    private double min;
    private double max;
    private double constValue;
    private ProbabilityDistribution distribution;
    public ConfiguredDoubleDistribution(double constValue)
    {
        this.constValue = constValue;
    }

    public ConfiguredDoubleDistribution(ProbabilityDistribution distribution, double min, double max)
    {
        this.min = min;
        this.max = max;
        this.distribution = distribution;
    }

    public double getConstValue()
    {
        return constValue;
    }

    public double getMin()
    {
        if (distribution == null)
        {
            return constValue;
        }
        else
        {
            return min;
        }
    }

    public void setMin(double min)   //TODO control min/max
    {
        this.min = min;
    }

    public double getMax()
    {
        if (distribution == null)
        {
            return constValue;
        }
        else
        {
            return max;
        }
    }

    public void setMax(double max)      //TODO control min/max
    {
        this.max = max;
    }

    public double nextDouble()
    {
        if (distribution == null)
        {
            return constValue;
        }
        else
        {
            return distribution.nextDouble(min, max);
        }
    }

    @Override
    public ProbabilityDistribution getDistribution()
    {
        return distribution;
    }
}
