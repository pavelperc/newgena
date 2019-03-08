package org.processmining.models.base_bpmn;

import org.processmining.models.Tokenable;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public interface SequenceFlow<T extends Token> extends Tokenable<T>
{
    Flow getFlow();
}
