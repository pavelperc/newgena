package com.pavelperc.newgena.gui.examplewithviewmodel

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.ArrayStringConverter
import com.pavelperc.newgena.gui.views.ArrayEditor
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
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
                
                field("friends") {
                    val list = person.friends.value
                    val textProp = SimpleStringProperty(list.joinToString("; "))

//                    list.addListener { c: ListChangeListener.Change<out String> ->
//                        textProp.value = list.joinToString("; ")
//                    }
                    
                    val splitPattern = Pattern.compile("""\s*[;,]\s*""")
                    textProp.addListener { observable, oldValue, newValue ->
                        val splitted = newValue
                                .trim('[', ']', '{', '}')
                                .trimIndent()
                                .split(splitPattern)
                                .toMutableList()
                        list.setAll(splitted)
                    }
                    
                    textfield(textProp)
                    
                    button(graphic = Styles.expandIcon()) {
                        action {
                            val arrayEditor = ArrayEditor(person.friends.value.toList(), onSuccess = { changedObjects ->
                                textProp.value = changedObjects.joinToString("; ")
                            })
                            
                            arrayEditor.openModal()
                        }
                    }
                }
                
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
