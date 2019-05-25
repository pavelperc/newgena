package com.pavelperc.newgena.gui.examplewithviewmodel

import com.pavelperc.newgena.gui.model.bindList
import com.pavelperc.newgena.gui.model.bindMap
import com.pavelperc.newgena.loaders.settings.reflectionToString
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
import kotlin.reflect.KMutableProperty

class Body {
    enum class Skin { White, Black }

    var skin = Skin.White
    var height = 25.0


    override fun toString() = reflectionToString(this)
}

class Person {


    //        val nameProperty = SimpleStringProperty("Oleg")
//        var name by nameProperty
    var name = "Oleg"

    var surname: String? = null

//        val ageProperty = SimpleIntegerProperty(25)
//        var age by ageProperty

    var age = 25

    var isUsingBody = true

    var friends = mutableListOf("Ivan", "Alex")
    
    var friendAges = mutableMapOf("A" to 1, "B" to 2)

    // todo: nullable body
    var body: Body = Body()
    
    override fun toString() = reflectionToString(this)
}

fun <T> KMutableProperty<T>.toPropAny(toString: (T) -> String, fromString: (String) -> T): SimpleStringProperty {
    val prop = SimpleStringProperty(toString(call()))
    prop.addListener { observable, oldValue, newValue ->
        setter.call(fromString(newValue))
    }
    return prop
}

fun KMutableProperty<Int>.toPropInt() = toPropAny(Int::toString, String::toInt)
fun KMutableProperty<Double>.toPropDouble() = toPropAny(Double::toString, String::toDouble)


class PersonModel(person: Person) : ItemViewModel<Person>(person) {

    val name = bind(Person::name)
    val age = bind(Person::age)

    val surname = bind(Person::surname)

    val isUsingBody = bind(Person::isUsingBody)

    val body = bind(Person::body)

    val bodyModel = BodyModel(person.body)

    
    
    
    val friends = bindList(Person::friends)
    val friendAges = bindMap(Person::friendAges)
    
    

    override fun onCommit() {
        super.onCommit()
        bodyModel.commit()
    }
}

class BodyModel(body: Body) : ItemViewModel<Body>(body) {

    val skin = bind(Body::skin)
    val height = bind { item::height.toPropDouble() }
}
