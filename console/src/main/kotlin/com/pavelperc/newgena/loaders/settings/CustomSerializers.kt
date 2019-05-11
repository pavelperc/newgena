package com.pavelperc.newgena.loaders.settings

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.time.Instant

/** Serialize NoiseEvent.activity as any. */
object ActivitySerializer: KSerializer<Any> {
    override val descriptor = StringDescriptor.withName("ActivitySerializer")
    
    override fun deserialize(decoder: Decoder) = decoder.decodeString()
    
    override fun serialize(encoder: Encoder, obj: Any) {
        encoder.encodeString(obj.toString())
    }
}

@Serializer(forClass = Instant::class)
object InstantSerializer: KSerializer<Instant> {
    override val descriptor = StringDescriptor.withName("InstantSerializer")
    
    override fun deserialize(decoder: Decoder) = Instant.parse(decoder.decodeString())!!
    
    override fun serialize(encoder: Encoder, obj: Instant) {
        encoder.encodeString(obj.toString())
    }
}

