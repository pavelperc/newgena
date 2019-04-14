package com.pavelperc.newgena.gui.model

import com.pavelperc.newgena.loaders.settings.*
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
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

abstract class NestingItemViewModel<T>(initial: T?) : ItemViewModel<T>(initial) {
    
    protected val innerModelsToProps = mutableMapOf<ItemViewModel<Any>, KProperty1<T, Any>>()
    
    
    fun <F, M : ItemViewModel<out F>> bindModel(prop: KProperty1<T, F>, kmodel: KClass<M>): M {
        
        val model = kmodel.primaryConstructor!!.call(prop.call(item))
        
        innerModelsToProps[model as ItemViewModel<Any>] = prop as KProperty1<T, Any>
        return model
    }
    
    init {
        itemProperty.onChange { newItem ->
            innerModelsToProps.entries.forEach { (model, prop) ->
                model.itemProperty.set(newItem?.let { prop.call(it) })
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
    
    val isUsingStaticPriorities = bind(JsonSettings::isUsingStaticPriorities) as BooleanProperty
    
    val isUsingTime = bind(JsonSettings::isUsingTime) as BooleanProperty
//    val timeDescription = bind(JsonSettings::timeDescription)
    
    // ---INNER MODELS:
    val petrinetSetupModel = bindModel(JsonSettings::petrinetSetup, PetrinetSetupModel::class)
    val staticPrioritiesModel = bindModel(JsonSettings::staticPriorities, StaticPrioritiesModel::class)
    val noiseDescription = bindModel(JsonSettings::noiseDescription, NoiseModel::class)
}

class PetrinetSetupModel(initial: JsonPetrinetSetup)
    : NestingItemViewModel<JsonPetrinetSetup>(initial) {
    
    val petrinetFile = bind(JsonPetrinetSetup::petrinetFile)
    
    val inhibitorArcIds = bindList(JsonPetrinetSetup::inhibitorArcIds)
    
    val resetArcIds = bindList(JsonPetrinetSetup::resetArcIds)
    
    // ---INNER MODELS:
    val markingModel = bindModel(JsonPetrinetSetup::marking, MarkingModel::class)
}


class MarkingModel(initial: JsonMarking)
    : ItemViewModel<JsonMarking>(initial) {
    
    val isUsingInitialMarkingFromPnml = bind(JsonMarking::isUsingInitialMarkingFromPnml)
    val initialPlaceIds = bindMap(JsonMarking::initialPlaceIds)
    val finalPlaceIds = bindMap(JsonMarking::finalPlaceIds)
}


class StaticPrioritiesModel(initial: JsonStaticPriorities)
    : NestingItemViewModel<JsonStaticPriorities>(initial) {
    
    val maxPriority = bind(JsonStaticPriorities::maxPriority) // >= 1
    val transitionIdsToPriorities = bind(JsonStaticPriorities::transitionIdsToPriorities, forceObjectProperty = true)
}


class NoiseModel(initial: JsonNoise)
    : NestingItemViewModel<JsonNoise>(initial) {
    
    val noiseLevel = bind(JsonNoise::noiseLevel) // in 1..100
    
    val isSkippingTransitions = bind(JsonNoise::isSkippingTransitions)
    
    val isUsingExternalTransitions = bind(JsonNoise::isUsingExternalTransitions)
    val isUsingInternalTransitions = bind(JsonNoise::isUsingInternalTransitions)
    
    val internalTransitionIds = bindList(JsonNoise::internalTransitionIds)
    
    val artificialNoiseEvents = bindList(JsonNoise::artificialNoiseEvents) // NoiseEvent class
}









