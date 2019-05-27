package com.pavelperc.newgena.utils.common

import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.semantics.petrinet.Marking
import java.io.File
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


fun markingOf(vararg places: Place) = Marking(places.asList())

fun markingOf(vararg places: Pair<Place, Int>) = Marking(places.flatMap { (place, count) -> List(count) { place } })
fun markingOf(places: Map<Place, Int>) = Marking(places.flatMap { (place, count) -> List(count) { place } })

fun emptyMarking() = Marking()


fun profile(message:String, op: () -> Unit) {
    println("$message <<<<")
    val time = measureNanoTime(op)
    println(">>>> ${String.format("%.2f", time / 1000_000.0)}ms")
}

fun getCwd() = System.getProperty("user.dir")
fun getCwdFile() = File(getCwd())

fun findCollisions(set1: Set<String>, set2: Set<String>) {
    set2 - set1
}

fun <T> Collection<T>.randomOrNull() = if (isEmpty()) null else random()

