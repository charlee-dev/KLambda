import com.squareup.moshi.JsonAdapter
import component.common.createJson
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
private object FunctionJsonAdapterFactory : JsonAdapter.Factory by KotshiFunctionJsonAdapterFactory

val functionJson = createJson(FunctionJsonAdapterFactory)
