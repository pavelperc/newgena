package com.pavelperc.newgena.graphviz

import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Compass
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Factory.*
import org.junit.Test
import java.io.File


class JustGraphvizTest {
    
    
    @Test
    fun immutableStyle() {
        val g = graph("example1").directed()
                .graphAttr().with(RankDir.LEFT_TO_RIGHT)
                .with(
                        node("a").with(Color.RED).link(node("b")),
                        node("b").link(Factory.to(node("c")).with(Style.DASHED))
                )
        Graphviz.fromGraph(g).height(100).render(Format.SVG).toFile(File("gv/immutable_simple.svg"))
    }
    
    @Test
    fun immutableStyleComplex() {
        val main = node("main").with(Label.html("<b>main</b><br/>start"), Color.rgb("1020d0").font())
        val init = node(Label.of("**_init_**"))
        val execute = node("execute")
        val compare = node("compare").with(Shape.RECTANGLE, Style.FILLED, Color.hsv(.7, .3, 1.0))
        val mkString = node("mkString").with(Label.of("make a\nstring"))
        val printf = node("printf")
        
        val g = graph("example2").directed().with(
                main.link(
                        Factory.to(node("parse").link(execute)), //.with(LinkAttr.weight(8)),
                        Factory.to(init).with(Style.DOTTED),
                        Factory.node("cleanup"),
                        Factory.to(printf).with(Style.BOLD, Label.of("100 times"), Color.RED)),
                execute.link(
                        graph().with(mkString, printf),
                        Factory.to(compare).with(Color.RED)),
                init.link(mkString))
    
        Graphviz.fromGraph(g).width(900).render(Format.SVG).toFile(File("gv/immutable_complex.svg"))
    }
    
    @Test
    fun kotlinStyle1() {
        graph(directed = true, name = "example") {
            edge["color" eq "red", Arrow.TEE]
            node[Color.GREEN]
            graph[RankDir.LEFT_TO_RIGHT, "label" eq "example graph"]
            
            
            "a" - "b" - "c"
            ("c"[Color.RED] - "d"[Color.BLUE])[Arrow.VEE]
            "d" / Compass.NORTH - "e" / Compass.SOUTH
        }.toGraphviz().render(Format.SVG).toFile(File("gv/kotlin_style.svg"))
    }
    
    @Test
    fun kotlinStyle2() {
        val graphviz = graph(directed = true) {
            graph[RankDir.LEFT_TO_RIGHT]
            
            "a"[Color.RED] - ("b" - "c")[Style.DASHED]
            
            
            mutNode("d")
        }.toGraphviz()
        
        graphviz.render(Format.SVG).toFile(File("gv/kotlin2.svg"))
    }
}