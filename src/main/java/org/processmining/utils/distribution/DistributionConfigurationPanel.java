package org.processmining.utils.distribution;

import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;

/**
 * Created by Ivan on 07.12.2015.
 */
public abstract class DistributionConfigurationPanel extends EmptyPanel
{
    public abstract ConfiguredDoubleDistribution getConfiguredDistribution();
}
