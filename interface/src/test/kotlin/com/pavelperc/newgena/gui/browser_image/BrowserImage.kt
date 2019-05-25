package com.pavelperc.newgena.gui.browser_image

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.ImageViewer
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.io.File


class WebViewApp : App(MyWebView::class, Styles::class) {
}



class MyWebView : View("MyWebView") {
    
    
    override val root = vbox {
        val path = "examples\\petrinet\\simpleExample\\simple.svg"
        ImageViewer(SimpleObjectProperty(File(path))).apply {
            attachTo(this@vbox)
        }
    }
}

fun main(args: Array<String>) {
    launch<WebViewApp>()
}