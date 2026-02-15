import model.FileMetadata

suspend fun main() {
    val client = Config.createClient()
    val metadata: FileMetadata = getFileMetadata(client, Config.url)
    client.close()
}
