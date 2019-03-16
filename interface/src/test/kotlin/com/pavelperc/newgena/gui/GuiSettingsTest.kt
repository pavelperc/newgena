package com.pavelperc.newgena.gui

import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.models.markInhResetArcsByIds
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeInt
import javafx.util.StringConverter
import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import tornadofx.*

class GuiSettingsTest : App(UISettings::class) {
    
    
    class Person {
        class Body {
            enum class Skin { White, Black }
            
            val skin = Skin.White
            val height = 25.0
            
            
            
            override fun toString() = "Body(skin=$skin, height=$height)"
        }
        
        //        val nameProperty = SimpleStringProperty("Oleg")
//        var name by nameProperty
        var name = "Oleg"
        
        var surname: String? = null

//        val ageProperty = SimpleIntegerProperty(25)
//        var age by ageProperty
        
        var age by NonNegativeInt(25)
        
        var isUsingBody = true
        
        var body: Body? = Body()
        
        override fun toString() =
                "Person(name='$name', surname=$surname, body=$body, age=$age)"
        
        
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
                
                myStringField(p::name)
                myStringField(p::surname)
                
                myIntField(p::age)
                
                
                MyStringField(p::name).attachTo(this)
                MyStringFieldNullable(p::surname).attachTo(this)
                MyIntField(p::age).attachTo(this)
                
                MyBooleanField(p::isUsingBody).attachTo(this)
                
//                field("body") {
//                    textfield(pm.body, )
//                }
                
                
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
        launch<GuiSettingsTest>()
    }
}