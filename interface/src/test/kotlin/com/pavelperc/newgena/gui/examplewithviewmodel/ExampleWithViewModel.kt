package com.pavelperc.newgena.gui.examplewithviewmodel

import com.pavelperc.newgena.gui.app.Styles
import org.junit.Test
import tornadofx.*

class ExampleWithViewModel : App(MainView::class, Styles::class) {
    
    @Test
    fun test() {
        launch<ExampleWithViewModel>()
    }
}