package com.pavelperc.newgena.gui.customfields

import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import tornadofx.*

fun Pane.scrollableFieldset(op: Fieldset.() -> Unit) {
    scrollpane {
        val scrollPane = this
        form {
            alignment = Pos.CENTER
            fieldset {
                alignment = Pos.CENTER
                //        prefHeightProperty().bind(scrollPane.heightProperty()) 
//        prefWidthProperty().bind(scrollPane.prefViewportWidthProperty())
//        prefWidthProperty().bind(scrollPane.minViewportWidthProperty())
                scrollPane.isFitToWidth = true
                op()
            }
        }
    }
}

private fun TitledPane.expandOn(prop: Property<Boolean>) {
    // one direction binding
    prop.onChange { checked -> this.isExpanded = checked!! }
}

fun EventTarget.foldingFieldSet(
        name: String,
        /** One direction binding: [expandOn] affects folding.
         * But folding doesn't affect [expandOn].*/
        expandOn: Property<Boolean>? = null,
        isNewForm: Boolean = true,
        op: Fieldset.() -> Unit = {}
) {
    squeezebox {
        fold(name, expandOn?.value ?: false) {
            if (expandOn != null) {
                expandOn(expandOn)
            }
            if (isNewForm) {
                form {
                    style {
                        padding = box(0.px)
                    }
                    fieldset {
                        style {
                            padding = box(10.px, 10.px, 0.px, 10.px)
                        }
                        op()
                    }
                }
            } else {
                fieldset {
                    style {
                        padding = box(10.px, 10.px, 0.px, 10.px)
                        spacing = 10.px
                    }
                    op()
                }
            }
            
        }
    }
}