package app.inspiry.core.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
public abstract class JsonContentPolymorphicSerializer<T : Any>(private val baseClass: KClass<T>) :
    KSerializer<T> {
    /**
     * A descriptor for this set of content-based serializers.
     * By default, it uses the name composed of [baseClass] simple name,
     * kind is set to [PolymorphicKind.SEALED] and contains 0 elements.
     *
     * However, this descriptor can be overridden to achieve better representation of custom transformed JSON shape
     * for schema generating/introspection purposes.
     */
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("JsonContentPolymorphicSerializer<${baseClass.simpleName}>", PolymorphicKind.SEALED)

    final override fun serialize(encoder: Encoder, value: T) {
        val actualSerializer = selectSerializer(value)
        @Suppress("UNCHECKED_CAST")
        actualSerializer.serialize(encoder, value)
    }

    final override fun deserialize(decoder: Decoder): T {
        val input = decoder as JsonDecoder
        val tree = input.decodeJsonElement()

        @Suppress("UNCHECKED_CAST")
        val actualSerializer = selectDeserializer(tree) as KSerializer<T>
        return input.json.decodeFromJsonElement(actualSerializer, tree)
    }

    /**
     * Determines a particular strategy for deserialization by looking on a parsed JSON [element].
     */
    protected abstract fun selectDeserializer(element: JsonElement): DeserializationStrategy<out T>
    protected abstract fun selectSerializer(value: T): SerializationStrategy<T>


}
