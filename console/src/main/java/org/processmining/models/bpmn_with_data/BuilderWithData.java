package org.processmining.models.bpmn_with_data;

/**
 * Created by Ivan on 04.03.2016.
 */
public interface BuilderWithData
{
    void inputDataObject(LoggableStringDataObject inputDataObject);

    void outputDataObject(LoggableStringDataObject outputDataObject);
}
