package component.common.util

import java.util.Base64
import kotlin.experimental.xor

fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)

internal fun String.fromBase64(): ByteArray = Base64.getDecoder().decode(this)

internal infix fun ByteArray.xor(other: ByteArray) = zip(other) { a, b -> a xor b }.toByteArray()
