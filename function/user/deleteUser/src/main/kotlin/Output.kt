import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Output(
    val deleted: Boolean,
) {
    internal companion object {
        val outputLens = functionJson.autoBody<Output>().toLens()
        val success = Output(true)
        val failure = Output(false)
    }
}
