package org.processmining.models.base_bpmn;

import org.processmining.models.Tokenable;
import org.processmining.models.abstract_net_representation.Token;

/**
 * Created by Ivan on 03.09.2015.
 */
public interface MessageFlow<T extends Token> extends Tokenable<T>
{
    org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow getFlow();
}
