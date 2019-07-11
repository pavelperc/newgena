package com.pavelperc.newgena.utils.common

import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.semantics.petrinet.Marking
import java.io.File
import java.util.*
import kotlin.random.Random
import kotlin.system.measureNanoTime


fun markingOf(vararg places: Place) = Marking(places.asList())

fun markingOf(vararg places: Pair<Place, Int>) = Marking(places.flatMap { (place, count) -> List(count) { place } })
fun markingOf(places: Map<Place, Int>) = Marking(places.flatMap { (place, count) -> List(count) { place } })

fun emptyMarking() = Marking()


fun <T> profile(message: String, op: () -> T) : T {
    println("$message <<<<")
    val start = System.nanoTime()
    val res = op()
    val time =  System.nanoTime() - start
    
    println(">>>> ${String.format("%.2f", time / 1000_000.0)}ms")
    return res
}

fun getCwd() = System.getProperty("user.dir")
fun getCwdFile() = File(getCwd())

/** Overrides append(string) */
operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}

fun <T> Collection<T>.randomOrNull(random: Random = Random) = if (isEmpty()) null else random(random)

fun <K, V> TreeMap<K, V>.firstValue() = firstEntry()?.value


fun drawTable(
        heads: List<String>,
        rows: List<List<String>>,
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