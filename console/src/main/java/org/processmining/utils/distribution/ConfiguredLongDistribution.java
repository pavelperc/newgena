package org.processmining.utils.distribution;

/**
 * Created by Ivan on 27.11.2015.
 */
public class ConfiguredLongDistribution implements ConfiguredDistribution
{
    private ProbabilityDistribution distribution;
    private long min;
    private long max;
    private long constValue;

    public ConfiguredLongDistribution(long constValue)
    {
        this.constValue = constValue;
    }

    public ConfiguredLongDistribution(ProbabilityDistribution distribution, long min, long max)
    {
        this.distribution = distribution;
        this.min = min;
        this.max = max;
    }

    public long getMin()
    {
        return min;
    }

    public long getMax()
    {
        return max;
    }

    public long nextLong()
    {
        if (distribution == null)
        {
            return constValue;
        }
        else
        {
            return distribution.nextLong(min, max);
        }
    }

    @Override
    public ProbabilityDistribution getDistribution()
    {
        return distribution;
    }
}
