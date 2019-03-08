package org.processmining.plugins.bpmn;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.models.EventLogArray;
import org.processmining.models.descriptions.BasicBPMNGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.plugins.bpmn.diagram.BpmnDiagram;
import org.processmining.plugins.bpmn.parameters.BpmnSelectDiagramParameters;
import org.processmining.plugins.bpmn.plugins.BpmnImportPlugin;
import org.processmining.utils.BPMNLoggingSingleton;
import org.processmining.utils.Generator;
import org.processmining.utils.ProgressBarCallback;
import org.processmining.utils.helpers.SimpleBPMNHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ivan on 05.02.2016.
 */
public class TestModels
{
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Plugin
            (
                    name = "Test BPMN models",
                    returnLabels = {},
                    returnTypes = {},
                    parameterLabels = {}
            )
    @UITopiaVariant
            (
                    /*affiliation = "Higher School of Economics",
                    email = "shugurov94@gmail.com",
                    author = "Ivan Shugurov"   */
                    affiliation = "", email = "", author = "")
    public void testModels(UIPluginContext context) throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException
    {
        File directoryWithModels = new File("C:\\Users\\Ivan\\Desktop\\process mining\\models\\models");
        File directoryWithBadModels = new File("C:\\Users\\Ivan\\Desktop\\process mining\\models\\bad models");
        File modelsWeCanWorkWith = new File("C:\\Users\\Ivan\\Desktop\\process mining\\models\\can generate");
        File emptyLog = new File("C:\\Users\\Ivan\\Desktop\\process mining\\models\\empty log");
        File modelsWeCannotWorkWith = new File("C:\\Users\\Ivan\\Desktop\\process mining\\models\\cannot generate");

        int totalFiles = 0;
        int notImported = 0;
        int importedIncorrectly = 0;
        int cannotExecute = 0;
        int canExecute = 0;
        int withEmptyTraces = 0;

        BpmnImportPlugin importPlugin = new BpmnImportPlugin();
        Method importMethod = BpmnImportPlugin.class.getDeclaredMethod("importBpmnFromStream", PluginContext.class, InputStream.class, String.class, long.class);
        importMethod.setAccessible(true);

        for (File file : directoryWithModels.listFiles())
        {
            totalFiles++;

            try (InputStream inputStream = new FileInputStream(file))
            {
                Bpmn bpmn = (Bpmn) importMethod.invoke(importPlugin, context, inputStream, "", 0);

                if (bpmn == null)
                {
                    notImported++;
                    System.out.println(file.getName() + " - not imported");
                }
                else
                {
                    try
                    {
                        List<BpmnDiagram> diagrams = new ArrayList<>(bpmn.getDiagrams());
                        BpmnDiagram diagram = diagrams.get(0);

                        BPMNDiagram newDiagram = BPMNDiagramFactory.newBPMNDiagram("");
                        Map<String, BPMNNode> id2node = new HashMap<>();
                        Map<String, Swimlane> id2lane = new HashMap<>();

                        if (diagram == BpmnSelectDiagramParameters.NODIAGRAM)
                        {
                            bpmn.unmarshall(newDiagram, id2node, id2lane);
                        }
                        else
                        {
                            Collection<String> elements = diagram.getElements();
                            bpmn.unmarshall(newDiagram, elements, id2node, id2lane);
                        }

                        //System.out.println(file.getName() + " - imported correctly");

                        try
                        {
                            EventLogArray array = generateLogs(newDiagram);

                            boolean emptyTraces = true;

                            for (int i = 0; i < array.getSize(); i++)
                            {
                                if (!array.getLog(i).isEmpty())
                                {
                                    emptyTraces = false;
                                    break;
                                }
                            }

                            if (emptyTraces)
                            {
                                withEmptyTraces++;

                                try
                                {
                                    String path = emptyLog + "\\" + file.getName();
                                    Files.copy(file.toPath(), Paths.get(path));
                                }
                                catch (Exception e)
                                {

                                }
                            }
                            else
                            {
                                 canExecute++;

                                try
                                {
                                    String path = modelsWeCanWorkWith + "\\" + file.getName();
                                    Files.copy(file.toPath(), Paths.get(path));
                                } catch (Exception e)
                                {

                                }
                            }

                        } catch (Exception e)
                        {
                            cannotExecute++;
                            String pathAsString = modelsWeCannotWorkWith + "\\" + file.getName();
                            Files.copy(file.toPath(), Paths.get(pathAsString));

                            String text = e.getMessage();
                            if (text != null)
                            {
                                pathAsString = pathAsString.replace("xml", "txt");
                                Files.write(Paths.get(pathAsString), text.getBytes());
                            }
                        }
                    } catch (Exception exception)
                    {
                        importedIncorrectly++;
//                        String path = directoryWithBadModels + "\\" + file.getName();
//                        Files.copy(file.toPath(), Paths.get(path));
                    }
                }
            }

            System.out.println(totalFiles);
        }

        System.out.println("total: " + totalFiles);
        System.out.println("not imported: " + notImported);
        System.out.println("imported incorrectly: " + importedIncorrectly);
        System.out.println("cannot execute: " + cannotExecute);
        System.out.println("can execute: " + canExecute);
        System.out.println("generated empty traces: " + withEmptyTraces);
    }

    private EventLogArray generateLogs(final BPMNDiagram diagram) throws InterruptedException
    {
        BasicBPMNGenerationDescription description = new BasicBPMNGenerationDescription(!diagram.getSwimlanes().isEmpty() || !diagram.getPools().isEmpty());

        BPMNLoggingSingleton.init(description.isUsingResources());

        SimpleBPMNHelper helper = SimpleBPMNHelper.createSimpleHelper(diagram, description);
        Generator generator = new Generator(new ProgressBarCallback()
        {
            @Override
            public void increment()
            {

            }
        });

        return generator.generate(helper);
    }
}
