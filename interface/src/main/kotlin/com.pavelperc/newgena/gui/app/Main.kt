package com.pavelperc.newgena.gui.app

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.gui.views.SettingsView
import com.pavelperc.newgena.loaders.settings.JsonSettings
import tornadofx.*

class MyApp: App(SettingsView::class, Styles::class)


fun main(args: Array<String>) {
    launch<MyApp>(args)
}