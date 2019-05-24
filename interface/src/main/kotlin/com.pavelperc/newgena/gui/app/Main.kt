package com.pavelperc.newgena.gui.app

import com.pavelperc.newgena.gui.views.settings.SettingsView
import javafx.scene.Scene
import tornadofx.*

class MyApp: App(SettingsView::class, Styles::class) {
    
    companion object {
        const val WINDOW_WIDTH = 700.0
        const val WINDOW_HEIGHT = 900.0
    }
    
    
    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, WINDOW_WIDTH, WINDOW_HEIGHT)
    
//    override fun start(stage: Stage) {
//        super.start(stage)
////        stage.width = 500.0
//        stage.height = 1000.0
//    }
}


fun main(args: Array<String>) {
    if (args.size > 0) {
        println("Starting console with settings path: ${args[0]}")
        com.pavelperc.newgena.main(args)
    } else {
        println("Hello, starting gui.")
        launch<MyApp>(args)
    }
}