package org.processmining.utils.distribution;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Normal;

/**
 * Created by Ivan on 11.08.2015.
 */
public class NormalDistribution extends AbstractDistribution
{
    private Normal distribution;
    private double mean;
    private double standardDeviation;

    public NormalDistribution(double mean, double standardDeviation)
    {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        distribution = new Normal(mean, standardDeviation, getGenerator());
    }

    public static NormalDistributionConfigurationPanel getConfigurationPanel()
    {
        return new NormalDistributionConfigurationPanel();
    }

    public static NormalDistributionConfigurationPanel getConfigurationPanel(double mean, double standardDeviation, double min, double max)
    {
        return new NormalDistributionConfigurationPanel(mean, standardDeviation, min, max);
    }

    @Override
    protected AbstractContinousDistribution initDistribution()
    {
        return distribution;
    }

    public Distributions getDistributionType()
    {
        return Distributions.NORMAL;
    }

    public double getMean()
    {
        return mean;
    }

    public void setMean(double mean)
    {
        this.mean = mean;
        distribution.setState(this.mean, this.standardDeviation);
    }

    public double getStandardDeviation()
    {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation)
    {
        this.standardDeviation = standardDeviation;
        distribution.setState(this.mean, this.standardDeviation);
    }
}
