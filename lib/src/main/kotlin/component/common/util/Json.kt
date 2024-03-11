package component.common.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
private object BeNattyJsonAdapterFactory : JsonAdapter.Factory by KotshiBeNattyJsonAdapterFactory

fun createJson(factory: JsonAdapter.Factory) = ConfigurableMoshi(
    Moshi.Builder()
        .add(factory)
        .add(ListAdapter)
        .add(MapAdapter)
        .asConfigurable()
        .withStandardMappings()
        .done()
)

// configure JSON AutoMarshalling without reflection, via Kotshi
internal val libJson = createJson(BeNattyJsonAdapterFactory)
