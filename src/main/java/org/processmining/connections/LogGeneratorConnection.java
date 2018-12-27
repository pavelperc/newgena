package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/**
 * @author Ivan Shugurov
 *         Created on 09.02.14
 */

public class LogGeneratorConnection extends AbstractConnection
{
    public static String PETRINET = "petrinet";
    public static String GENERATION_DESCRIPTION = "generation description";

    public LogGeneratorConnection(String label, Petrinet petrinet,
                                  TimeDrivenGenerationDescription generationDescription)
    {
        super(label);
        put(PETRINET, petrinet);
        put(GENERATION_DESCRIPTION, generationDescription);
    }

    public TimeDrivenGenerationDescription getGenerationDescription()
    {
        return (TimeDrivenGenerationDescription) get(GENERATION_DESCRIPTION);
    }

}
