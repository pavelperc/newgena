package com.pavelperc.newgena.gui.app

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val itemRoot by cssclass()
        val closeIcon by cssclass()
        val contentLabel by cssid()
        val title by cssid()
        val addItemRoot by cssclass()
        
        val greenButton by cssclass()
        val redButton by cssclass()
        
        fun closeIcon() = FontAwesomeIconView(FontAwesomeIcon.CLOSE).apply {
            glyphSize = 22
            addClass(closeIcon)
        }
        
        fun expandIcon() = FontAwesomeIconView(FontAwesomeIcon.EXPAND).apply {
            //            addClass(closeIcon)
        }
        
    }
    
    init {
        itemRoot {
            padding = box(8.px)
            button {
                backgroundColor += c("transparent")
                padding = box(-2.px)
            }
            alignment = Pos.CENTER_LEFT
        }
        
        contentLabel {
            fontSize = 1.2.em
        }
        
        title {
            fontSize = 3.em
            textFill = c(175, 47, 47, 0.5)
        }
        
        addItemRoot {
            label {
                padding = box(0.5.em)
            }
            
            padding = box(1.em)
            textField {
                prefWidth = 200.px
            }
        }
        
        greenButton {
            backgroundColor += Color.GREEN.brighter()
        }
        
        redButton {
            baseColor = Color.RED.brighter()
//            backgroundColor += Color.RED
//            and(hover) {
//                backgroundColor += Color.RED.brighter().brighter()
//            }
        }
        
    }
}