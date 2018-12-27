package org.processmining.utils.distribution;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;

/**
 * Created by Ivan on 11.08.2015.
 */
public class UniformDistribution extends AbstractDistribution
{
    private Uniform uniform = new Uniform(getGenerator());

    public UniformDistribution()
    {
    }

    public static UniformDistributionConfigurationPanel getConfigurationPanel()
    {
        return new UniformDistributionConfigurationPanel();
    }

    public static UniformDistributionConfigurationPanel getConfigurationPanel(double min, double max)
    {
        return new UniformDistributionConfigurationPanel(min, max);
    }

    public static ConstantConfigurationPanel getConstantConfigurationPanel()
    {
        return new ConstantConfigurationPanel();
    }

    public static ConstantConfigurationPanel getConstantConfigurationPanel(double constValue)
    {
        return new ConstantConfigurationPanel(constValue);
    }

    @Override
    public long nextLong(long min, long max)
    {
        checkMinMax(min, max);
        return uniform.nextLongFromTo(min, max);
    }

    @Override
    public double nextDouble(double min, double max)
    {
        return uniform.nextDoubleFromTo(min, max);
    }

    @Override
    protected AbstractContinousDistribution initDistribution()
    {
        return uniform;
    }


    @Override
    public long nextLong()
    {
        return uniform.nextLongFromTo(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public Distributions getDistributionType()
    {
        return Distributions.UNIFORM;
    }

}
