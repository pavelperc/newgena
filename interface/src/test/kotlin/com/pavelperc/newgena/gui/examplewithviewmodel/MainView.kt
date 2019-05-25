package com.pavelperc.newgena.gui.examplewithviewmodel

import com.pavelperc.newgena.gui.customfields.*
import tornadofx.*

class MainView : View("Main View") {
    override val root = Form()
    
    
    override fun onBeforeShow() {
        super.onBeforeShow()
        println("onBeforeShow")
        person.validate()
    }
    
    
    val person = PersonModel(Person())
    val body = person.bodyModel
    
    init {
        with(root) {
            fieldset {
                field("name") {
                    textfield(person.name).required()
                }
                
                intField(person.age, nextValidator = { newValue ->
                    when {
                        newValue < 0 -> error("Age should not be negative.")
                        else -> null
                    }
                })
                
                
                val nameRegex = Regex("""[A-Z][a-z]*""")
                
                val possibleFriends = mutableMapOf(
                        "Vanya" to "good boy",
                        "Misha" to "bad boy",
                        "Tolya" to "something between"
                )
                
                
                arrayField(person.friends, { possibleFriends }) { list ->
                    list.firstOrNull { !it.matches(nameRegex) }?.let {
                        error("Name $it doesn't match ${nameRegex.pattern}.")
                    }
                }
                
                intMapField(person.friendAges, predefinedValuesToHints = {
                    person.friends.value.map { it to possibleFriends[it] }.toMap()
                })
                
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
            
            
            println("Validator props:")
            println(person.validationContext.validators.map { it.property.viewModelFacade?.name })
            
        }
    }
    
}
