package org.processmining.utils.python;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Ivan on 12.01.2016.
 */
public class PythonRunner
{
    private PythonRunner()
    {

    }

    public static String run(String path)
    {
        return run(path, "");
    }

    public static String run(String path, String arguments)
    {
        try
        {
            return privateRun(path, arguments);
        } catch (IOException | InterruptedException e)
        {
            throw new PythonExecutionException("Could not execute script", e);
        }
    }

    private static String privateRun(String path, String arguments) throws IOException, InterruptedException
    {
        Process process = Runtime.getRuntime().exec("python " + path + " " + arguments);
        int exitCode = process.waitFor();

        if (exitCode == 0)
        {
            try (Scanner scanner = new Scanner(process.getInputStream()))
            {
                return scanner.nextLine();
            }
        }
        else
        {
            throw new PythonExecutionException("Python script exited with error");
        }
    }
}
