package com.pavelperc.newgena.graphviz

import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import com.pavelperc.newgena.utils.common.plusAssign
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableGraph
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc
import org.processmining.models.semantics.petrinet.Marking
import java.io.File


class PetrinetDrawer(
        val petrinet: PetrinetGraph,
        val initialMarking: Marking = Marking(),
        val finalMarking: Marking = Marking(),
        val graphLabelStr: String = petrinet.label,
        val drawLegend: Boolean = true,
        val drawArcIds: Boolean = true,
        val drawPlaceIds: Boolean = true,
        val drawTransitionIds: Boolean = false,
        val drawTransitionNames: Boolean = true,
        val drawVertical: Boolean = false,
        val drawFinalMarking: Boolean = true
) {
    // circles
    private val circle = "●"
//        val circle = "•"
//        val circle = "*"
    
    fun makeGraph(saveToSvg: String? = null) = convert().also { graph ->
        if (saveToSvg != null)
            graph.toGraphviz().render(Format.SVG).toFile(File(saveToSvg))
    }
    
    
    private fun convert(): MutableGraph = graph(directed = true) {
        graph[Rank.MAX]
        if (drawVertical) {
            graph["size" eq "10,10"]
            graph["overlap" eq "compress"]
//            graph["scale" eq "10.0"]
            graph["ratio" eq "compress"]
            graph[RankDir.TOP_TO_BOTTOM]
        } else {
            graph[RankDir.LEFT_TO_RIGHT]
        }
//        graph["label" eq graphLabelStr]
        val labelHtml = StringBuilder(graphLabelStr)
        if (drawLegend) {
            if (labelHtml.isNotBlank()) {
                labelHtml += "<br/>"
            }
            labelHtml += "$circle - initial marking"
            if (drawFinalMarking) {
                labelHtml += ", <font color=\"lightseagreen\">$circle</font>  - final marking"
            }
        }
    
    
        // this condition fixes a strange error when labelHtml is empty
        // and than the graphviz crashes because of the transition label!!
        if (labelHtml.isNotBlank()) {
            graph[Label.html(labelHtml.toString())]
        }
        
        
        val places = petrinet.places.map {
            val label = Label.of(it.pnmlId)
            val node = mutNode(label).add(Shape.CIRCLE)
            if (drawPlaceIds) {
                node[label.external()]
            }
            node
        }
        
        petrinet.transitions.forEach {
            var labelStr = ""
            if (drawTransitionNames) labelStr += it.label
            if (drawTransitionNames && drawTransitionIds) labelStr += "\n"
            if (drawTransitionIds) labelStr += it.pnmlId
            mutNode(it.pnmlId).add(
                    Shape.RECTANGLE,
//                    Label.of("${it.label}(${it.pnmlId})")
                    Label.raw(labelStr)
            )
        }
        
        // drawing arcs
        petrinet.edges.forEach { edge ->
            
            val labelStr =
                    if (drawArcIds) "${edge.label}(${edge.pnmlId})"
                    else if (edge.label == "1" || edge !is Arc) ""
                    else edge.label
            
            val arc = (edge.source.pnmlId - edge.target.pnmlId)[Label.of(labelStr)]
            when (edge) {
                is InhibitorArc -> arc[Arrow.DOT.open()]
                is ResetArc -> arc[Arrow.NORMAL.and(Arrow.NORMAL)]
            }
        }
        
        // reset default labels.
        places.forEach { it[Label.raw("   ")] }
        drawTokens(initialMarking.toList())
        if (drawFinalMarking) {
            drawTokens(finalMarking.toList(), true)
        }
    }
    
    private fun drawTokens(marking: List<Place>, isFinalMarking: Boolean = false) {
        // grouping places by labels
        val counts = marking.groupBy { it.pnmlId }.mapValues { it.value.size }
        counts.forEach { pnmlId, count ->
            val node = mutNode(pnmlId)
            
            val points = when {
                count > 3 -> "$count $circle"
//                    else -> circle.repeat(count).chunked(3).joinToString("<br/>")
                else -> circle.repeat(count)
            }
            
            val oldLabel = (node.attrs()["label"] as? Label)?.value() ?: ""
            var label = oldLabel.trim(' ')
            if (label != "")
                label += "<br/>"
            
            if (isFinalMarking) {
                label += "<font color=\"lightseagreen\">$points</font>"
            } else {
                label += "<font color=\"black\">$points</font>"
            }
            
            node[Label.html(label)]
        }
    }
} 