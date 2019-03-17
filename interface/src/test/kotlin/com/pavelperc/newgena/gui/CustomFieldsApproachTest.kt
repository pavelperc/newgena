package com.pavelperc.newgena.gui

import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeInt
import org.junit.Test
import tornadofx.*

class CustomFieldsApproachTest : App(UISettings::class) {
    
    
    class Person {
        class Body {
            enum class Skin { White, Black }
            
            var skin = Skin.White
            var height = 25
            
            override fun toString() = "Body(skin=$skin, height=$height)"
        }
        
        //        val nameProperty = SimpleStringProperty("Oleg")
//        var name by nameProperty
        var name = "Oleg"
        
        var surname: String? = null

//        val ageProperty = SimpleIntegerProperty(25)
//        var age by ageProperty
        
        var age by NonNegativeInt(25)
        
        var friends: MutableList<String>? = mutableListOf("Friend1", "Friend2")
        
        var isUsingBody = true
        
        var body: Body = Body()
        
        override fun toString() =
                "Person(name='$name', surname=$surname, body=$body, age=$age, isUsingBody=$isUsingBody, friends=$friends)"
        
        
    }


//    class PersonModel(person: Person) : ItemViewModel<Person>(person) {
//        val name = bind(Person::name)
//        val age = bind(Person::age)
//        val body = bind(Person::body)
//    }
    
    class UISettings : View() {
        val p = Person()
//        val pm = PersonModel(p)
        
        override val root = form {
            fieldset {
                
//                myStringField(p::name)
//                myStringField(p::surname)
//                myIntField(p::age)
                
                myStringField(p::name)
                
                myStringFieldNullable(p::surname)
                myIntField(p::age)
                
                myBooleanField(p::isUsingBody)
                
                myStringArrayFieldNullable(p::friends)
                
//                field("body") {
//                    textfield(pm.body, )
//                }
                
                myComplexPropertyFieldSet(p::body) { body ->
                    myIntField(body::height)
                    myEnumField(body::skin)
                }
                
                button("print") {
                    action {
                        println(p)
                    }
                    
                }
                
            }
        }
    }
    
    
    @Test
    fun testCustomFieldExtensions() {
        launch<CustomFieldsApproachTest>()
        
        
    }
}