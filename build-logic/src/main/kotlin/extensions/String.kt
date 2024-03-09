package extensions

import java.util.Locale

internal fun String.asGroup(): String {
    return this
        .replace(":", ".")
        .replace("'", "")
        .replace("-", "")
        .replace("_", "")
        .replace("project ", "")
        .lowercase(Locale.ROOT)
}
