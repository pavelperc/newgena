package com.pavelperc.newgena.gui.browser_image

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.examplewithviewmodel.MainView
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Parent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.web.WebView
import javafx.util.StringConverter
import org.junit.Test
import tornadofx.*
import org.apache.http.util.EntityUtils.consume




class WebViewApp : App(MyWebView::class, Styles::class) {
    
    
}

class MyWebView : View("MyWebView") {
    
    val zoomProp = SimpleDoubleProperty(1.0)
    
    lateinit var webView: WebView
    
    override val root = vbox {
        style {
            backgroundColor += Color.RED
        }
        
        hbox {
            button("ZoomIn") {
                action {
                    zoomProp.value += 0.1
                }
            }
            button("ZoomOut") {
                action {
                    zoomProp.value -= 0.1
                }
            }
            button("Reset") {
                action {
                    zoomProp.value = 1.0
                }
            }
            label(zoomProp, converter = object : StringConverter<Number>() {
                override fun toString(obj: Number) = "Zoom factor: %.2f".format(obj)
                override fun fromString(string: String) = 0.0
            })
        }
        
        webView = webview {
            
//            widthProperty().onChange { 
//                println("pref: $prefWidth, max=$maxWidth, actual: $width")
//                
//            }
            
            setOnScroll { event ->
                if (event.isControlDown) {
                    var zoomFactor = 1.05
                    val deltaY = event.deltaY
                    if (deltaY < 0) {
                        zoomFactor = 2.0 - zoomFactor
                    }
                    println("zoomed: $zoomFactor, deltaY=$deltaY")
                    zoomProp.value *= zoomFactor
                    
                    event.consume()
                }
            }
            
            
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            zoomProperty().bind(zoomProp)
            
            val cwd = System.getProperty("user.dir")
            val path = "file:///$cwd\\examples\\petrinet\\simpleExample\\simple.svg"
            println(path)
            engine.load(path)
        }
    }
}

fun main(args: Array<String>) {
    launch<WebViewApp>()
}