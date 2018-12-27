package org.processmining.plugins;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.GenerationDescription;
import org.processmining.utils.Generator;
import org.processmining.utils.ProgressBarCallback;
import org.processmining.utils.helpers.GenerationHelper;

/**
 * Created by Ivan on 16.05.2016.
 */
public class GeneratorWrapper
{
    public static EventLogArray generate(GenerationHelper helper, GenerationDescription description, UIPluginContext context)
    {
        final Progress progress = context.getProgress();

        progress.setMinimum(0);
        progress.setMaximum(description.getNumberOfLogs() * description.getNumberOfTraces());
        progress.setValue(0);

        ProgressBarCallback callback = new ProgressBarCallback()
        {
            @Override
            public void increment()
            {
                progress.inc();
            }
        };

        return new Generator(callback).generate(helper);
    }
}
