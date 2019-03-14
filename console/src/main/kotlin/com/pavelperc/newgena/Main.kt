package com.pavelperc.newgena

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.settings.JsonSettingsController
import com.pavelperc.newgena.utils.xlogutils.eventNames
import com.pavelperc.newgena.utils.xlogutils.exportXml
import com.pavelperc.newgena.utils.xlogutils.toList
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.descriptions.TimeDrivenGenerationDescription

fun main(args: Array<String>) {
    
    println("Hello world!!")
    println("Look at tests!")
    println("Working directory: ${System.getProperty("user.dir")}")
    println("Loading settings")
    
    val settingsFilePath =
            if (args.size > 0) args[0]
            else "examples/petrinet/simpleExample/settings.json"
    
    val settingsController = JsonSettingsController.createFromFilePath(settingsFilePath)
    settingsController.updateInhResetArcsFromSettings()
    
    val generationKit = settingsController.getGenerationKit()
    
    
    println("Settings were built successfully!")
    
    
    // TODO: pavel: make GenerationController instead of PetrinetGenerators and hide this awful code there.
    val logArray = with(generationKit) {
//        petrinet.toGraphviz(initialMarking, saveToSvg = "gv/simpleExample/simple.svg")
        
        when (description) {
            is SimpleGenerationDescription -> PetrinetGenerators.generateSimple(
                    petrinet,
                    initialMarking,
                    finalMarking,
                    description)
            is GenerationDescriptionWithStaticPriorities -> PetrinetGenerators.generateWithPriorities(
                    petrinet,
                    initialMarking,
                    finalMarking,
                    description)
            is TimeDrivenGenerationDescription -> PetrinetGenerators.generateWithTime(
                    petrinet, // TODO: pavel ADAPT TIMEDRIVEN TO INHIBITOR AND RESET NETS.
                    initialMarking,
                    finalMarking,
                    description)
            
            else -> throw IllegalStateException("Unsupported type of generation description")
        }
    }
//    logArray.toList().map { it.eventNames() }.joinToString("\n").also { println(it) }
    
    with(settingsController) {
        logArray.exportXml("${jsonSettings.outputFolder}/${petrinet.label}.xes")
    }
    
    println("Done!")
}