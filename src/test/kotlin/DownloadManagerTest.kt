import config.Config
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import utils.DownloadManager
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals

class DownloadManagerTest {

    @Test
    fun `download coordinates all components correctly`() = runBlocking {
        val tempFile = Files.createTempFile("download-test", ".txt")
        try {
            val content = "Hello, World! This is a test file content."
            val contentBytes = content.toByteArray()
            val url = "http://example.com/testfile"

            val mockEngine = MockEngine { request ->
                if (request.method == HttpMethod.Head) {
                    respond(
                        content = "",
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentLength to listOf(contentBytes.size.toString()),
                            HttpHeaders.AcceptRanges to listOf("bytes")
                        )
                    )
                } else if (request.method == HttpMethod.Get) {
                    val range = request.headers[HttpHeaders.Range]
                    if (range != null && range.startsWith("bytes=")) {
                        val parts = range.substring(6).split("-")
                        val start = parts[0].toInt()
                        val end = parts[1].toInt()
                        val rangeContent = contentBytes.sliceArray(start..end)
                        respond(
                            content = rangeContent,
                            status = HttpStatusCode.PartialContent
                        )
                    } else {
                        respond(
                            content = contentBytes,
                            status = HttpStatusCode.OK
                        )
                    }
                } else {
                    respond(content = "Not Found", status = HttpStatusCode.NotFound)
                }
            }

            Config.client = HttpClient(mockEngine)
            Config.url = url
            val downloadManager = DownloadManager(Config.client)

            downloadManager.download(url, tempFile)

            val downloadedContent = Files.readAllBytes(tempFile)
            assertContentEquals(contentBytes, downloadedContent)

        } finally {
            Files.deleteIfExists(tempFile)
        }
    }
}
