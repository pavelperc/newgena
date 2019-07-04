package com.pavelperc.newgena.utils.common

import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.semantics.petrinet.Marking
import java.io.File
import kotlin.system.measureNanoTime


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

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}



fun <T> Collection<T>.randomOrNull() = if (isEmpty()) null else random()


fun drawTable(
        heads: List<String>,
        rows : List<List<String>>,
        alignRight: Boolean = false,
        spacing: Int = 2
): String {
    val builders = List(rows.size + 1) { StringBuilder() }
    
    heads.withIndex().forEach { (i, head) ->
        val column = listOf(head) + rows.map { row -> row.getOrNull(i) ?: "" }
        
        val maxSize = column.map { it.length }.max() ?: 0
        
        column.withIndex().forEach { (j, cell) ->
            if (alignRight) {
                builders[j].append(cell.padStart(maxSize + spacing))
            } else {
                builders[j].append(cell.padEnd(maxSize + spacing))
            }
        }
    }
    return builders.joinToString("\n") { it.toString() }
}