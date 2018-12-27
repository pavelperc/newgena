package org.processmining.utils.distribution;

/**
 * Created by Ivan on 11.08.2015.
 */
public interface ProbabilityDistribution
{
    long nextLong();
    long nextLong(long min, long max);

    double nextDouble();
    double nextDouble(double min, double max);
}
