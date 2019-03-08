package com.pavelperc.newgena

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.PnmlLoader
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.fromJson
import com.pavelperc.newgena.loaders.settings.mapper
import org.deckfour.xes.out.XesXmlSerializer
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import java.io.File

fun main(args: Array<String>) {
    
    println("Hello world!!")
    println("Look at tests!")
    println("Working directory: ${System.getProperty("user.dir")}")
    println("Loading settings")
    
    val settingsFilePath =
            if (args.size > 0) args[0]
            else "examples/petrinet/simpleExample/settings.json"
    val jsonSettingsStr = File(settingsFilePath).readText()
    
    val jsonSettings = JsonSettings.fromJson(jsonSettingsStr)
    
    val (petrinet, pnmlMarking) = PnmlLoader.loadPetrinet(jsonSettings.petrinetFile)
    
    val builder = JsonSettingsBuilder(petrinet, jsonSettings)
    
    val generationDescription = builder.buildDescription()
    var (initialMarking, finalMarking) = builder.buildMarking()
    
    if (jsonSettings.marking.isUsingInitialMarkingFromPnml) {
        initialMarking = pnmlMarking
    }
    
    println("Settings were built successfully!")
    
    val logArray = when (generationDescription) {
        is SimpleGenerationDescription -> PetrinetGenerators.generateSimple(
                petrinet,
                initialMarking,
                finalMarking,
                generationDescription)
        is GenerationDescriptionWithStaticPriorities -> PetrinetGenerators.generateWithPriorities(
                petrinet,
                initialMarking,
                finalMarking,
                generationDescription)
        is TimeDrivenGenerationDescription -> PetrinetGenerators.generateWithTime(
                petrinet, // TODO: pavel ADAPT TIMEDRIVEN TO INHIBITOR AND RESET NETS.
                initialMarking,
                finalMarking,
                generationDescription)
        
        else -> throw IllegalStateException("Unsupported type of generation description")
        
    }
    val serializer = XesXmlSerializer()
    val logFile = File("${jsonSettings.outputFolder}/${petrinet.label}.xes")
    logFile.parentFile.mkdirs()
    logArray.exportToFile(null, logFile, serializer)
    println("Done!")
}