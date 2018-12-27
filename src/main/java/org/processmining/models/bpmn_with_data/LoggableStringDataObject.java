package org.processmining.models.bpmn_with_data;

import org.processmining.utils.python.PythonRunner;

/**
 * Created by Ivan on 15.04.2015.
 */
public class LoggableStringDataObject extends LoggableDataObject
{
    private String value;
    private String scriptPath;

    public LoggableStringDataObject(String dataObjectName)
    {
        super(dataObjectName);
        checkValue(dataObjectName);
    }

    public String getScriptPath()
    {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath)
    {
        this.scriptPath = scriptPath;
    }

    @Override
    public String read()
    {
        return value;
    }

    public void write(String value)
    {
        checkValue(value);

        this.value = value;
    }

    public void moveToInitialState()
    {
        if (scriptPath == null)
        {
            value = "";
        }
        else
        {
            value = PythonRunner.run(scriptPath + " object", getLabel());
        }
    }

    private void checkValue(String value)
    {
        if (value == null)
        {
            throw new NullPointerException("String parameter cannot be equal to null");
        }
    }
}
