package com.pavelperc.newgena.gui.model

import com.pavelperc.newgena.loaders.settings.JsonMarking
import com.pavelperc.newgena.loaders.settings.JsonPetrinetSetup
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonStaticPriorities
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import tornadofx.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor


/** Binds mutableList to [ItemViewModel] as observableList property.*/
fun <T, S> ItemViewModel<T>.bindList(prop: KMutableProperty1<T, out List<S>>) =
        bind {
            SimpleObjectProperty(null, prop.name, item?.let { prop.call(it) }?.observable() ?: observableList())
        }


/** Binds mutableMap to [ItemViewModel] as observableMap property.*/
fun <T, K, V> ItemViewModel<T>.bindMap(prop: KMutableProperty1<T, out MutableMap<K, V>>) =
        bind(forceObjectProperty = true) {
            //            SimpleObjectProperty(null, prop.name, item?.let { prop.call(it) }?.observable() ?: FXCollections.observableHashMap())
            val fxProp = SimpleObjectProperty(null, prop.name, item?.let { prop.call(it) } ?: mutableMapOf())
            
            fxProp.onChange { map -> item?.let { prop.setter.call(it, map ?: mutableMapOf<K, V>()) } }
            fxProp
        }

abstract class NestingItemViewModel<T>(initial: T) : ItemViewModel<T>(initial) {
    
    protected val innerModelsToProps = mutableMapOf<ItemViewModel<Any>, KProperty1<T, Any>>()
    
    
    fun <F, M: ItemViewModel<F>> bindModel(prop: KProperty1<T, F>, kmodel: KClass<M>): M {
        
        val model = kmodel.primaryConstructor!!.call(prop.call(item!!))
        innerModelsToProps[model as ItemViewModel<Any>] = prop as KProperty1<T, Any>
        return model
    }
    
    init {
        itemProperty.onChange { newItem ->
            innerModelsToProps.entries.forEach { (model, prop) ->
                model.itemProperty.set(newItem?.let { prop.call(it) } )
            }
        }
    }
    
    override fun onCommit() {
        super.onCommit()
        innerModelsToProps.keys.forEach { it.commit() }
    }
}


/**
 * A middleman between [JsonSettings] and ui.
 * Binds each property to observable, tracks validation,
 * can be reused for several [JsonSettings] objects.
 */
class SettingsModel(initial: JsonSettings) : NestingItemViewModel<JsonSettings>(initial) {
    
    val outputFolder = bind(JsonSettings::outputFolder)
    
    val numberOfLogs = bind(JsonSettings::numberOfLogs)
    val numberOfTraces = bind(JsonSettings::numberOfTraces)
    val maxNumberOfSteps = bind(JsonSettings::maxNumberOfSteps)
    
    val isRemovingEmptyTraces = bind(JsonSettings::isRemovingEmptyTraces)
    val isRemovingUnfinishedTraces = bind(JsonSettings::isRemovingUnfinishedTraces)
    
    val isUsingNoise = bind(JsonSettings::isUsingNoise)
//    val noiseDescription = bind(JsonSettings::noiseDescription)
    
    val isUsingStaticPriorities = bind(JsonSettings::isUsingStaticPriorities) as BooleanProperty
//    val staticPriorities = bind(JsonSettings::staticPriorities)
    
    val isUsingTime = bind(JsonSettings::isUsingTime) as BooleanProperty
//    val timeDescription = bind(JsonSettings::timeDescription)
    
    // ---INNER MODELS:
//    val petrinetSetupModel = PetrinetSetupModel(item.petrinetSetup)
    val petrinetSetupModel = bindModel(JsonSettings::petrinetSetup, PetrinetSetupModel::class)
    
    
    
//    override fun onCommit() {
//        super.onCommit()
//        petrinetSetupModel.commit()
//    }
//    
//    init {
//        
//        // todo: move to separate abstract class.
//        itemProperty.onChange { newItem ->
//            petrinetSetupModel.itemProperty.set(newItem?.petrinetSetup)
//        }
//    }
}

class PetrinetSetupModel(initial: JsonPetrinetSetup)
    : ItemViewModel<JsonPetrinetSetup>(initial) {
    
    val petrinetFile = bind(JsonPetrinetSetup::petrinetFile)
    
    val inhibitorArcIds = bindList(JsonPetrinetSetup::inhibitorArcIds)
    
    val resetArcIds = bindList(JsonPetrinetSetup::resetArcIds)
    
    // ---INNER MODELS:
    val markingModel = MarkingModel(initial.marking)
    
    override fun onCommit() {
        println("PetrinetSetupModel committed")
        super.onCommit()
        markingModel.commit()
    }
    
    init {
        itemProperty.onChange { newItem ->
            markingModel.itemProperty.set(newItem?.marking)
        }
    }
}


class MarkingModel(initial: JsonMarking)
    : ItemViewModel<JsonMarking>(initial) {
    
    val isUsingInitialMarkingFromPnml = bind(JsonMarking::isUsingInitialMarkingFromPnml)
    val initialPlaceIds = bindMap(JsonMarking::initialPlaceIds)
    val finalPlaceIds = bindMap(JsonMarking::finalPlaceIds)
}


class StaticPrioritiesModel(initial: JsonStaticPriorities)
    : ItemViewModel<JsonStaticPriorities>(initial) {
    
    
}






