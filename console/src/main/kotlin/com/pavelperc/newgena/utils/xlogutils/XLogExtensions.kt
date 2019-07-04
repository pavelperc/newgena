package com.pavelperc.newgena.utils.xlogutils

import org.deckfour.xes.extension.std.XConceptExtension
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.factory.XFactoryBufferedImpl
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.out.XesXmlSerializer
import org.processmining.log.models.EventLogArray
import java.io.File
import java.lang.IllegalStateException
import java.util.*


private val factory = XFactoryBufferedImpl()

var XEvent.name
    get() = attributes["concept:name"].toString()
    set(value) {
        val name = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance())
        attributes["concept:name"] = name
    }

val XEvent.time: Date
    get() = XTimeExtension.instance().extractTimestamp(this)
            ?: throw IllegalStateException("No timestamp was found in event $name")


val XEvent.resource
    get() = attributes["org:resource"].toString()

val XEvent.role
    get() = attributes["org:role"].toString()

val XEvent.group
    get() = attributes["org:group"].toString()

val XEvent.timestampStr
    get() = attributes["time:timestamp"].toString()

val XEvent.lifecycle
    get() = attributes["lifecycle:transition"].toString()

fun XEvent.printInRow() = "$name\t$group:$role:$resource\t$lifecycle\t$timestampStr"

fun XTrace.printEvents() = "name\tgroup:role:resource\tlifecycle\ttimestampStr\n" +
        joinToString("\n") { event -> event.printInRow() }

var XTrace.name
    get() = attributes["concept:name"].toString()
    set(value) {
        val name = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance())
        attributes["concept:name"] = name
    }


fun XTrace.eventNames() = map { event -> event.name }


fun XLog.eventNames() = map { trace -> trace.eventNames() }

fun EventLogArray.toSeq(): Sequence<XLog> = sequence {
    repeat(size) { i ->
        yield(getLog(i))
    }
}

fun EventLogArray.toList() = this.toSeq().toList()

/** Returns a list with all traces, containing only event names.*/
fun EventLogArray.eventNames() = toList().flatMap { log -> log.eventNames() }


fun EventLogArray.exportXml(filePath: String) {
    val serializer = XesXmlSerializer()
    val logFile = File(filePath)
    val folder = logFile.parentFile
    folder.deleteRecursively()
    folder.mkdirs()
    
    this.exportToFile(null, logFile, serializer)
}
