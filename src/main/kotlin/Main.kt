import java.nio.file.Paths

suspend fun main() {
    val downloadManager = DownloadManager(Config.client)

    try {
        val destination = Paths.get("downloaded-file.txt")
        println("Starting download from ${Config.url} to $destination")
        downloadManager.download(Config.url, destination)
        println("Successfully downloaded file.")
    } catch (e: Exception) {
        println("Download failed: ${e.message}")
    } finally {
        Config.client.close()
    }
}
