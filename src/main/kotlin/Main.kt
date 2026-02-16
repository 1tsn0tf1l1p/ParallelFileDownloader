import config.Config
import utils.DownloadManager
import java.nio.file.Paths
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

suspend fun main() {
    val downloadManager = DownloadManager(Config.client)

    try {
        val url = Config.url
        val fileName = url.substringAfterLast("/").substringBefore("?").ifEmpty { "downloaded-file" }
        val destination = Paths.get(fileName)
        
        logger.info("Starting download from {} to {}", url, destination)
        downloadManager.download(url, destination)
        logger.info("Successfully downloaded file to {}", destination)
    } catch (e: Exception) {
        logger.error("Download failed: {}", e.message, e)
    } finally {
        Config.client.close()
    }
}
