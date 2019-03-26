package com.pavelperc.newgena.testutils

import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.toGraphviz
import org.junit.AfterClass
import org.junit.BeforeClass
import java.io.File

/** Drawing helper for test classes. */
abstract class GraphvizDrawer(draw: Boolean = false) {
    
    // init is run between @BeforeClass and @AfterClass
    init {
//        println("inside GraphvizDrawer init")
        drawGraphviz = draw
    }
    
    companion object {
        const val DRAW_ALL: Boolean = true
    
        /** Map of graphviz graphs to their short filenames for saving.*/
        val forDrawing = mutableMapOf<MutableGraph, String>()
    
        private const val folder = "../gv"
        
        // it is always reset to false in @AfterClass
        private var drawGraphviz = false
        
        @BeforeClass
        @JvmStatic
        fun clearGraphviz() {
//            println("inside beforeClass")
            forDrawing.clear()
        }
        
        @AfterClass
        @JvmStatic
        fun drawGraphviz() {
//            println("inside afterClass")
            
            if (drawGraphviz && DRAW_ALL) {
                println("drawing graphviz")
                var counter = 0
                forDrawing.forEach { graph, filename ->
                    graph.toGraphviz().render(Format.SVG).toFile(File("$folder/$filename"))
                    println("drew: $folder/filename (${++counter} from ${forDrawing.size})")
                }
            }
            drawGraphviz = false
        }
    }
}