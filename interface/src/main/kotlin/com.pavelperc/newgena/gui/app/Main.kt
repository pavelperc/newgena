package com.pavelperc.newgena.gui.app

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.loaders.settings.JsonSettings
import tornadofx.*

class MyApp: App(SettingsView::class)


class SettingsView: View() {
    
    val settings = SettingsModel(JsonSettings())
    
    override val root = vbox {
        button("Press me")
        label("Waiting")
    }
}


fun main(args: Array<String>) {
    launch<MyApp>(args)
}