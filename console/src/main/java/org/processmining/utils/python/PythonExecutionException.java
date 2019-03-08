package org.processmining.utils.python;

/**
 * Created by Ivan on 12.01.2016.
 */
public class PythonExecutionException extends RuntimeException
{
    public PythonExecutionException()
    {

    }

    public PythonExecutionException(String message)
    {
        super(message);
    }

    public PythonExecutionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PythonExecutionException(Throwable cause)
    {
        super(cause);
    }
}
