package com.pavelperc.newgena.utils.xlogutils

import org.deckfour.xes.extension.std.XConceptExtension
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.factory.XFactoryBufferedImpl
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.processmining.log.models.EventLogArray
import java.lang.IllegalStateException
import java.util.*


private val factory = XFactoryBufferedImpl()

var XEvent.name
    get() = attributes["concept:name"].toString()
    set(value) {
        val name = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance())
        attributes["concept:name"] = name
    }


var XTrace.name
    get() = attributes["concept:name"].toString()
    set(value) {
        val name = factory.createAttributeLiteral("concept:name", value, XConceptExtension.instance())
        attributes["concept:name"] = name
    }



val XEvent.time: Date
    get() = XTimeExtension.instance().extractTimestamp(this)
            ?: throw IllegalStateException("No timestamp was found in event $name")

fun EventLogArray.toSeq(): Sequence<XLog> = sequence {
    0.until(size).forEach { i -> yield(getLog(i)) }
}

fun EventLogArray.toList() = this.toSeq().toList()

fun XTrace.eventNames() = map { event -> event.name }
fun XLog.eventNames() = map { trace -> trace.eventNames() }

/** Returns a list with all traces, containing only event names.*/
fun EventLogArray.eventNames() = toList().flatMap { log -> log.eventNames() }
