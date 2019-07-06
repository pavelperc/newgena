package com.pavelperc.newgena.loaders.settings.documentation

import com.pavelperc.newgena.loaders.settings.jsonSettings.*
import com.pavelperc.newgena.utils.common.profile
import kotlin.reflect.full.findAnnotation

/** Annotated documentation. */
@Target(AnnotationTarget.PROPERTY)
annotation class Doc(val text: String)


/** Contains annotated [Doc] from [JsonSettings] and other classes , mapped by property name. */
val documentationByName = profile("Collecting documentation") {
    listOf(
            SettingsInfo::class,
            JsonSettings::class,
            JsonMarking::class,
            JsonPetrinetSetup::class,
            JsonNoise::class,
            JsonStaticPriorities::class,
            JsonTimeDrivenNoise::class,
            JsonTimeDescription::class,
            JsonTimeDescription.DelayWithDeviation::class,
            JsonResources.Group::class,
            JsonResources.Role::class,
            JsonResources.Resource::class,
            JsonResources.JsonResourceMapping::class
    )
            .flatMap { it.members }
            .map { prop -> prop.name to prop.findAnnotation<Doc>() }
            .filter { (_, doc) -> doc != null }
            .map { (name, doc) -> name to doc!!.text }
            .toMap()
}

