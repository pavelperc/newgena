package com.pavelperc.newgena.gui.model

import com.pavelperc.newgena.loaders.settings.JsonMarking
import com.pavelperc.newgena.loaders.settings.JsonPetrinetSetup
import com.pavelperc.newgena.loaders.settings.JsonSettings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1

fun <T> KMutableProperty<out List<T>>.listProp(): SimpleObjectProperty<ObservableList<T>> {
    // when the caller object is null????
    val list = this.call()?.observable() ?: FXCollections.observableArrayList()
    return SimpleObjectProperty(list, this.name)
}

/** Binds mutableList to [ItemViewModel] as observableList property.*/
fun <T, S> ItemViewModel<T>.bindList(prop: KMutableProperty1<T, out List<S>>) =
        bind {
            SimpleObjectProperty(null, prop.name, item?.let { prop.call(it) }?.observable() ?: observableList())
        }


/**
 * A middleman between [JsonSettings] and ui.
 * Binds each property to observable, tracks validation,
 * can be reused for several [JsonSettings] objects.
 */
class SettingsModel(initial: JsonSettings) : ItemViewModel<JsonSettings>(initial) {
    
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
    val petrinetSetupModel = PetrinetSetupModel(item.petrinetSetup)
    
    override fun onCommit() {
        super.onCommit()
        petrinetSetupModel.commit()
    }
    
    init {
        // todo: move to separate abstract class.
        itemProperty.onChange { newItem ->
            petrinetSetupModel.itemProperty.set(newItem?.petrinetSetup)
        }
    }
}

class PetrinetSetupModel(initial: JsonPetrinetSetup)
    : ItemViewModel<JsonPetrinetSetup>(initial) {
    
    val petrinetFile = bind(JsonPetrinetSetup::petrinetFile, autocommit = true)
    
    val inhibitorArcIds = bindList(JsonPetrinetSetup::inhibitorArcIds)
    
    val resetArcIds = bindList(JsonPetrinetSetup::resetArcIds)
    
    // ---INNER MODELS:
    val markingModel = MarkingModel(initial.marking)
    
    override fun onCommit() {
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
    val initialPlaceIds = bindList(JsonMarking::initialPlaceIds)
    val finalPlaceIds = bindList(JsonMarking::finalPlaceIds)
}



