import exceptions.ChunkDownloadException
import exceptions.ConnectionException
import exceptions.RequestTimeoutException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import model.Chunk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class DownloaderTest {

    @Test
    fun `downloadChunk throws ChunkDownloadException on non-success status`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 0, endByte = 100)

        val exception = assertThrows<ChunkDownloadException> {
            downloadChunk(client, "http://example.com", chunk)
        }

        assertEquals("Failed to download chunk 1. HTTP Status: 500 Internal Server Error", exception.message)
    }

    @Test
    fun `downloadChunk returns bytes on success`() = runBlocking {
        val expectedBytes = "hello".toByteArray()
        val mockEngine = MockEngine { request ->
            respond(
                content = expectedBytes,
                status = HttpStatusCode.OK
            )
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 0, endByte = 4)

        val result = downloadChunk(client, "http://example.com", chunk)

        assertEquals(expectedBytes.toList(), result.toList())
    }

    @Test
    fun `downloadChunk throws RequestTimeoutException on timeout`() = runBlocking {
        val mockEngine = MockEngine { request ->
            throw HttpRequestTimeoutException("url", null)
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 0, endByte = 100)

        val exception = assertThrows<RequestTimeoutException> {
            downloadChunk(client, "http://example.com", chunk)
        }
        
        assertEquals("Chunk 1 timed out for URL: http://example.com", exception.message)
    }

    @Test
    fun `downloadChunk throws ConnectionException on IOException`() = runBlocking {
        val mockEngine = MockEngine { request ->
            throw IOException("Network error")
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 0, endByte = 100)

        val exception = assertThrows<ConnectionException> {
            downloadChunk(client, "http://example.com", chunk)
        }
        
        assertEquals("Chunk 1 connection dropped for URL: http://example.com", exception.message)
    }

    @Test
    fun `downloadChunk uses correct URL`() = runBlocking {
        var capturedUrl: String? = null
        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString()
            respond(content = "data".toByteArray(), status = HttpStatusCode.OK)
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 0, endByte = -1)
        val targetUrl = "http://example.com/file"

        downloadChunk(client, targetUrl, chunk)

        assertEquals(targetUrl, capturedUrl)
    }

    @Test
    fun `downloadChunk sets Range header correctly`() = runBlocking {
        var capturedRange: String? = null
        val mockEngine = MockEngine { request ->
            capturedRange = request.headers[HttpHeaders.Range]
            respond(content = "data".toByteArray(), status = HttpStatusCode.PartialContent)
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 10, endByte = 20)

        downloadChunk(client, "http://example.com", chunk)

        assertEquals("bytes=10-20", capturedRange)
    }

    @Test
    fun `downloadChunk does not set Range header when endByte is -1`() = runBlocking {
        var capturedRange: String? = null
        val mockEngine = MockEngine { request ->
            capturedRange = request.headers[HttpHeaders.Range]
            respond(content = "data".toByteArray(), status = HttpStatusCode.OK)
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 1, startByte = 0, endByte = -1)

        downloadChunk(client, "http://example.com", chunk)

        assertNull(capturedRange)
    }
    @Test
    fun `downloadChunk returns bytes on 206 Partial Content success`() = runBlocking {
        val expectedBytes = "partial_data".toByteArray()
        val mockEngine = MockEngine { request ->
            respond(
                content = expectedBytes,
                status = HttpStatusCode.PartialContent // Simulating a real chunk response
            )
        }
        val client = HttpClient(mockEngine)
        val chunk = Chunk(id = 2, startByte = 1024, endByte = 2047)

        val result = downloadChunk(client, "http://example.com", chunk)

        assertEquals(expectedBytes.toList(), result.toList())
    }
}
