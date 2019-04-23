package com.pavelperc.newgena.gui.app

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.gui.views.SettingsView
import com.pavelperc.newgena.loaders.settings.JsonSettings
import javafx.scene.Scene
import javafx.stage.Stage
import tornadofx.*

class MyApp: App(SettingsView::class, Styles::class) {
    
    
    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, 700.0, 900.0)
    
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