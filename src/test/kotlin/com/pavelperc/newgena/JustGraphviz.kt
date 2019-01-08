package com.pavelperc.newgena

import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.RankDir
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Compass
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Factory.*
import org.junit.Test
import java.io.File


class JustGraphviz {
    
    
    @Test
    fun javaStyle() {
        val g = graph("example1").directed()
                .graphAttr().with(RankDir.LEFT_TO_RIGHT)
                .with(
                        node("a").with(Color.RED).link(node("b")),
                        node("b").link(Factory.to(node("c")).with(Style.DASHED))
                )
        Graphviz.fromGraph(g).height(100).render(Format.SVG).toFile(File("gv/java_style.svg"))
    }
    
    @Test
    fun kotlin2() {
        graph(directed = true) {
            graph[RankDir.LEFT_TO_RIGHT]
            
            "a"[Color.RED] - ("b" - "c")[Style.DASHED]
            
            
            mutNode("d")
        }.toGraphviz().render(Format.SVG).toFile(File("gv/kotlin2.svg"))
    }
    
    @Test
    fun kotlinStyleSimple() {
        graph(directed = true, name = "example") {
            edge["color" eq "red", Arrow.TEE]
            node[Color.GREEN]
            graph[RankDir.LEFT_TO_RIGHT]

            "a" - "b" - "c"
            ("c"[Color.RED] - "d"[Color.BLUE])[Arrow.VEE]
            "d" / Compass.NORTH - "e" / Compass.SOUTH
        }.toGraphviz().render(Format.SVG).toFile(File("gv/kotlin_style.svg"))
    }
}