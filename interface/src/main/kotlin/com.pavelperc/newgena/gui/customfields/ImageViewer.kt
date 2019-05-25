package com.pavelperc.newgena.gui.customfields

import javafx.beans.property.Property
import javafx.beans.property.SimpleDoubleProperty
import javafx.concurrent.Worker
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.web.WebView
import javafx.util.StringConverter
import tornadofx.*
import java.io.File


class ImageViewer(val imagePath: Property<File?>, val uiComponent: UIComponent) : VBox() {
    @Suppress("UNCHECKED_CAST")
    val zoomProp = SimpleDoubleProperty(1.0) as Property<Double>
    
    lateinit var webView: WebView
    
    private fun EventTarget.zoomPanel() {
        hbox {
            fun Button.hintShortcut(comb: String, hint: String = comb) = with(uiComponent) {
                shortcut(comb)
                tooltip(hint)
            }
            
            alignment = Pos.CENTER_LEFT
            button("ZoomIn") {
                action {
                    zoomProp.value += 0.1
                }
                hintShortcut("=", "+")
            }
            button("ZoomOut") {
                hintShortcut("-")
                action {
                    if (zoomProp.value > 0.1) {
                        zoomProp.value -= 0.1
                    }
                }
            }
            button("Reset") {
                hintShortcut("R")
                action {
                    zoomProp.value = 1.0
                }
            }
            button("adjust") {
                hintShortcut("A")
                action {
                    adjustSize()
                }
            }
            
            label(zoomProp, converter = object : StringConverter<Double>() {
                override fun toString(obj: Double) = "Zoom: %.2f".format(obj)
                override fun fromString(string: String) = 0.0
            })
        }
    }
    
    private fun draw() {
        val path = "file:///" + imagePath.value?.absolutePath ?: ""
        webView.engine.loadContent("""<html><body>
                <div id="mydiv">
                <img id="myimg" src=$path>
                </div>
                </body></html>""".trimIndent())
    }
    
    fun adjustSize() {
        try {
            val imageWidth = webView.engine
                    .executeScript("document.getElementById('myimg').width") as Int
//            val imageHeight = webView.engine
//                    .executeScript("document.getElementById('myimg').height") as Int
            
            val actualWidth = webView.engine
                    .executeScript("document.getElementById('mydiv').offsetWidth") as Int
//            val actualHeight = webView.engine
//                    .executeScript("document.getElementById('mydiv').offsetHeight") as Int
            
//            val coeff = if (imageWidth > imageHeight)
//                (actualWidth + 1) / (imageWidth + 10.0)
//            else (actualHeight + 1) / (imageHeight + 10.0)
            val coeff = (actualWidth + 1) / (imageWidth + 10.0)
            
            zoomProp.value *= coeff
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    init {
        useMaxSize = true
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        
        style {
            backgroundColor += Color.LIGHTGRAY
        }
        
        webView = webview {
            useMaxSize = true
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            
            setOnScroll { event ->
                if (event.isControlDown) {
                    var zoomFactor = 1.05
                    val deltaY = event.deltaY
                    if (deltaY < 0) {
                        zoomFactor = 2.0 - zoomFactor
                    }
                    zoomProp.value *= zoomFactor
                    
                    event.consume()
                }
            }
            
            engine.loadWorker.stateProperty().onChange { value ->
                if (value == Worker.State.SUCCEEDED) {
                    adjustSize()
                }
            }
            
            zoomProperty().bind(zoomProp)
        }
        zoomPanel()
        
        
        
        imagePath.onChange {
            draw()
        }
        draw()
    }
}