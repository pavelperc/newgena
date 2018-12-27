package org.processmining.models.bpmn_with_time;

import org.processmining.models.Movable;

/**
 * Created by Ivan on 02.09.2015.
 */
public interface MovableWithTime extends Movable
{
    /**
     * returns null if a movable object does not have a timestamp
     *
     * @return
     */
    Long getTimestamp();
}
