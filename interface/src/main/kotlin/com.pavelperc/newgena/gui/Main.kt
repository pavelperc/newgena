package com.pavelperc.newgena.gui

import tornadofx.*

class MyApp: App(MyView::class)

class MyView: View() {
    override val root = vbox {
        button("Press me")
        label("Waiting")
    }
}


fun main(args: Array<String>) {
    launch<MyApp>(args)
}