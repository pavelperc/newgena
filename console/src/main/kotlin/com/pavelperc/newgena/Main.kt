package com.pavelperc.newgena

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.settings.JsonSettingsController
import com.pavelperc.newgena.utils.xlogutils.exportXml

fun main(args: Array<String>) {
    
    println("Hello world!!")
    println("Look at tests!")
    println("Working directory: ${System.getProperty("user.dir")}")
    println("Loading settings")
    
    val settingsFilePath =
            if (args.size > 0) args[0]
            else "examples/petrinet/complex1/settings.json"
    
    val settingsController = JsonSettingsController.createFromFilePath(settingsFilePath)
    println("Read settings from file $settingsFilePath")
//    settingsController.updateInhResetArcsFromSettings()
    
    val generationKit = settingsController.getGenerationKit()
    
    println("Settings were built successfully!")
    
    val logArray = PetrinetGenerators.generateFromKit(generationKit) { progress, maxProgress ->
        println("progress: $progress from $maxProgress")
    }
    
//    logArray.toList().map { it.eventNames() }.joinToString("\n").also { println(it) }
    
    with(settingsController) {
        val xesFileStr = "${jsonSettings.outputFolder}/${generationKit.petrinet.label}.xes"
        println("Export xes at: $xesFileStr")
        logArray.exportXml(xesFileStr)
    }
    
    println("Done!")
}