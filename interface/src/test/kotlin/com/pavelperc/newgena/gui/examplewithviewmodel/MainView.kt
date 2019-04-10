package com.pavelperc.newgena.gui.examplewithviewmodel

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.arrayField
import com.pavelperc.newgena.gui.customfields.intMapField
import com.pavelperc.newgena.gui.views.ArrayEditor
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import ui.model.Body
import ui.model.Person
import ui.model.PersonModel
import java.util.regex.Pattern

class MainView : View("Main View") {
    override val root = Form()
    
    
    val person = PersonModel(Person())
    val body = person.bodyModel
    
    init {
        with(root) {
            
            fieldset {
                field("name") {
                    textfield(person.name).required()
                }
                field("age") {
                    textfield(person.age) {
                        validator { newValue ->
                            when {
                                newValue == null -> error("Null")
                                !newValue.isInt() -> error("Not an Int")
                                newValue.toInt() < 0 -> error("Age should be positive")
                                else -> null
                            }
                        }
                    }
                }
                
                arrayField(person.friends)
                
                intMapField(person.friendAges)
                
                field("isUsingBody") {
                    checkbox(property = person.isUsingBody)
                }
                
                fieldset("body") {
                    field("skin") {
                        combobox(body.skin, Body.Skin.values().toList())
                    }
                    field("height") {
                        textfield(body.height) {
                            validator { newValue ->
                                when {
                                    newValue == null -> error("Null")
                                    !newValue.isDouble() -> error("Not a Double")
                                    else -> null
                                }
                            }
                        }
                    }
                }
            }
            
            button("Save") {
                enableWhen(person.valid)
                action {
                    println("Committed: " + person.commit())
                    println(person.item)
                }
            }
            
            button("Print") {
                action {
                    //                    alert(Alert.AlertType.INFORMATION, "Person:", person.person.toString())
                    println(person.item)
                }
            }
            
            
        }
    }
    
}
