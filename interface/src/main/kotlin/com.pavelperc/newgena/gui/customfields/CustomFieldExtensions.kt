package com.pavelperc.newgena.gui.customfields

import impl.org.controlsfx.autocompletion.SuggestionProvider
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.stage.Window
import javafx.util.Duration
import javafx.util.StringConverter
import org.controlsfx.control.Notifications
import tornadofx.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import org.controlsfx.control.textfield.TextFields

// See more extensions in this package


class QuiteIntConverter : StringConverter<Int>() {
    override fun toString(obj: Int?) = obj.toString()
    override fun fromString(string: String?) = string?.toIntOrNull() ?: 0
}

object QuiteLongConverter : StringConverter<Long>() {
    override fun toString(obj: Long?) = obj.toString()
    override fun fromString(string: String?) = string?.toLongOrNull() ?: 0
}


typealias Validator<T> = ValidationContext.(T) -> ValidationMessage?


/** Fires onAction after completion. */
fun <T> TextField.actionedAutoCompletion(suggestions: List<T>) {
    TextFields.bindAutoCompletion(this, suggestions).apply {
        onAutoCompleted = EventHandler {
            this@actionedAutoCompletion.fireEvent(ActionEvent())
        }
    }
}


/** Fires onAction after completion. */
fun <T> TextField.actionedAutoCompletion(suggestionsProvider: () -> List<T>) {
    TextFields.bindAutoCompletion(this) { suggestionsProvider() }.apply {
        onAutoCompleted = EventHandler {
            this@actionedAutoCompletion.fireEvent(ActionEvent())
        }
    }
}

/** Fires onAction after completion. */
fun <T> TextField.actionedAutoCompletion(suggestionsProvider: SuggestionProvider<T>) {
    TextFields.bindAutoCompletion(this, suggestionsProvider).apply {
        onAutoCompleted = EventHandler {
            this@actionedAutoCompletion.fireEvent(ActionEvent())
        }
    }
}

fun UIComponent.notification(
        title: String = "",
        text: String = "",
        duration: Int = 1500,
        op: Notifications.() -> Unit = {}
) {
    val builder = Notifications.create()
    builder.owner(this.root)
    builder.title(title)
    builder.text(text)
    builder.position(Pos.TOP_CENTER)
    builder.hideAfter(Duration(duration.toDouble()))
    
    builder.op()
    builder.show()
}

fun Tooltip.delayHack(delayInMillis: Int) {
    try {
        val fieldBehavior = this.javaClass.getDeclaredField("BEHAVIOR")
        fieldBehavior.isAccessible = true
        val objBehavior = fieldBehavior.get(this)
        
        val fieldTimer = objBehavior.javaClass.getDeclaredField("activationTimer")
        fieldTimer.isAccessible = true
        val objTimer = fieldTimer.get(objBehavior) as Timeline
        
        objTimer.getKeyFrames().clear()
        objTimer.getKeyFrames().add(KeyFrame(Duration(delayInMillis.toDouble())))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
}

fun confirmed(
        header: String,
        content: String = "",
        confirmButton: ButtonType = ButtonType.OK,
        cancelButton: ButtonType = ButtonType.CANCEL,
        owner: Window? = null,
        title: String? = null
): Boolean {
    alert(Alert.AlertType.CONFIRMATION, header, content, confirmButton, cancelButton, owner = owner, title = title) {
        return it == confirmButton
    }
    return false // not clicked at all
}

inline fun confirmIf(
        condition: Boolean,
        header: String,
        content: String = "",
        confirmButton: ButtonType = ButtonType.OK,
        cancelButton: ButtonType = ButtonType.CANCEL,
        owner: Window? = null,
        title: String? = null,
        actionFn: () -> Unit
) {
    if (condition) {
        if (confirmed(header, content, confirmButton, cancelButton, owner, title)) {
            actionFn()
        }
    } else {
        actionFn()
    }
}


