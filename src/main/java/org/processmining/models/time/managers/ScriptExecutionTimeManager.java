package org.processmining.models.time.managers;

import org.processmining.utils.python.PythonRunner;

/**
 * Created by Ivan on 31.03.2016.
 */
public class ScriptExecutionTimeManager<T> implements ExecutionTimeManager<T>
{
    private String scriptPath;

    public ScriptExecutionTimeManager(String scriptPath)
    {
        this.scriptPath = scriptPath;
    }

    @Override
    public long getExecutionTime(T object, long startTime)
    {
        String arguments = " \"" +
                object.toString() +
                '"' +
                ' ' +
                startTime;

        return Long.parseLong(PythonRunner.run(scriptPath, arguments));
    }
}
