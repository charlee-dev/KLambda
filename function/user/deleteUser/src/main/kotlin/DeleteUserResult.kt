import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class DeleteUserResult(
    val deleted: Boolean,
) {
    companion object {
        val lens = functionJson.autoBody<DeleteUserResult>().toLens()
        val success = DeleteUserResult(true)
        val failure = DeleteUserResult(false)
    }
}
