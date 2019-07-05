package com.pavelperc.newgena.utils.xlogutils

import com.pavelperc.newgena.utils.common.drawTable
import org.deckfour.xes.extension.std.XConceptExtension
import org.deckfour.xes.factory.XFactoryBufferedImpl
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.out.XesXmlSerializer
import org.processmining.log.models.EventLogArray
import java.io.File
import java.time.Instant


private val factory = XFactoryBufferedImpl()

var XEvent.name
    get() = attributes["concept:name"].toString()
    set(value) {
        val name = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance())
        attributes["concept:name"] = name
    }

val XEvent.time: Long
    get() = Instant.parse(attributes["time:timestamp"].toString()).toEpochMilli()


val XEvent.resource
    get() = attributes["org:resource"]?.toString()

val XEvent.role
    get() = attributes["org:role"]?.toString()

val XEvent.group
    get() = attributes["org:group"]?.toString()

val XEvent.timestampStr
    get() = attributes["time:timestamp"].toString()

val XEvent.lifecycle
    get() = attributes["lifecycle:transition"].toString()


fun XTrace.drawEvents(
        startTime: Long = 0L,
        timeGranularity: Long = 1,
        drawLifeCycle: Boolean = true,
        drawRes: Boolean = true,
        drawTimestamp: Boolean = true,
        drawFullTimestamp: Boolean = false
        
): String {
    val heads = mutableListOf("name")
    if (drawRes) {
        heads += "group:role:resource"
    }
    if (drawLifeCycle) {
        heads += "lifecycle"
    }
    if (drawTimestamp) {
        heads += "timestamp/$timeGranularity"
    }
    if (drawFullTimestamp) {
        heads += "fullTimestamp"
    }
    val rows = map { event -> event.run { 
        val cells = mutableListOf(name)
        if (drawRes) {
            cells += "$group:$role:$resource"
        }
        if (drawLifeCycle) {
            cells += lifecycle
        }
        if (drawTimestamp) {
            cells += ((time - startTime) / timeGranularity).toString()
        }
        if (drawFullTimestamp) {
            cells += timestampStr
        }
        cells
    } }
    
    return drawTable(heads, rows)
}

var XTrace.name
    get() = attributes["concept:name"].toString()
    set(value) {
        val name = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance())
        attributes["concept:name"] = name
    }


fun XTrace.eventNames() = map { event -> event.name }


fun XLog.eventNames() = map { trace -> trace.eventNames() }

val EventLogArray.allTraces: List<XTrace>
    get() = this.toList().flatten()


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
