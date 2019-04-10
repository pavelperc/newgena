package com.pavelperc.newgena.gui.app

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import org.controlsfx.control.Notifications
import tornadofx.*

object ArtificialIntelligence {
    
    
    private fun randomBye(): Pair<String, String> {
        val list = listOf(
                "Buy a license!" to "I mean, Bye!!",
                "Goodbye!" to "Waiting for you again!!",
                "Bye!" to "We did it!!"
        )
        return list.random()
    }
    
    fun goodbye(primaryStage: Stage) {
        val owner = Stage(StageStyle.TRANSPARENT)
        val root = StackPane()
        root.style = "-fx-background-color: TRANSPARENT"
        val scene = Scene(root, 1.0, 1.0)
        scene.fill = Color.TRANSPARENT
        owner.scene = scene
        owner.width = 1.0
        owner.height = 1.0
        owner.toBack()
        owner.show()
        
        val (title, message) = randomBye()
        Notifications.create()
                .title(title)
                .text(message)
                .hideAfter(Duration(1800.0))
                .position(Pos.CENTER)
                .show()
        
        primaryStage.hide()
        
        runLater(Duration(1900.0)) {
            Platform.exit();
            System.exit(0);
        }
    }
}