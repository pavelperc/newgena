package com.pavelperc.newgena.gui.app

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
//        val itemRoot by cssclass()
        val deleteButton by cssclass()
        val intMapDeleteButton by cssclass()
        
        val contentLabel by cssid()
        val title by cssid()
        val addItemRoot by cssclass()
        
        val greenButton by cssclass()
        val redButton by cssclass()
        val fieldSetFrame by cssclass()
//        val intMapEditorItem by cssclass()
        val sortingPanel by cssclass()
        val upDownPanel by cssclass()
    
        fun closeIcon() = FontAwesomeIconView(FontAwesomeIcon.CLOSE).apply {
            glyphSize = 22
//            addClass(closeIcon)
            fill = Color.GRAY.brighter()
            hoverProperty().onChange { hover ->
                if (hover) {
                    fill = Color.BLACK
                } else {
                    fill = Color.GRAY.brighter()
                }
            }
        }
        
        fun expandIcon() = FontAwesomeIconView(FontAwesomeIcon.EXPAND).apply {
            //            addClass(closeIcon)
        }
        
    }
    
    init {
        val lightGrey = c("#E3E3E3")
        
//        itemRoot {
////            padding = box(8.px)
//            
//            alignment = Pos.CENTER_LEFT
//        }
//        
//        intMapEditorItem {
////            padding = box(8.px)
//            
//            alignment = Pos.CENTER_LEFT
//
////            textField {
////                backgroundColor += Color.TRANSPARENT
////            }
//        }
        
        
        
        deleteButton {
            backgroundColor += c("transparent")
            padding = box((-2).px)
        }
        
        intMapDeleteButton {
            backgroundColor += Color.TRANSPARENT
            padding = box(0.px, 0.px, 0.px, 12.px)

//            and(hover) {
//                backgroundColor += Color.GRAY.brighter().brighter()
//            }
//            and(pressed) {
//                backgroundColor += Color.GRAY.brighter()
//            }
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
            
//            padding = box(1.em, 1.em, 0.em, 1.em)
//            textField {
//                prefWidth = 200.px
//            }
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
        
        sortingPanel {
            fontSize = 1.em
            button {
                padding = box(1.px, 8.px)
                backgroundColor += Color.TRANSPARENT
                and(hover) {
                    backgroundColor += lightGrey
                }
            }
        }
        
        upDownPanel {
            alignment = Pos.CENTER_LEFT
            padding = box(0.px, 2.px, 0.px, 0.px)
            button {
                fontSize = 1.em
                padding = box(1.px, 2.px)
                backgroundColor += Color.TRANSPARENT
                and(hover) {
                    backgroundColor += lightGrey
                }
            }
        }
        
        fieldSetFrame {
            borderColor += box(Color.GRAY.brighter())
            borderRadius += box(5.px)
            borderWidth += box(1.px)
            borderInsets += box(4.px, 0.px)
            padding = box(4.px, 8.px)
        }
        
    }
}