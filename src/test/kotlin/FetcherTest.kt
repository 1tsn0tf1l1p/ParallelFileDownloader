import exceptions.ConnectionException
import exceptions.MetadataFetchException
import exceptions.RequestTimeoutException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class FetcherTest {

    @Test
    fun `getFileMetadata returns correct metadata when parallel support is present`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentLength to listOf("1024"),
                    HttpHeaders.AcceptRanges to listOf("bytes")
                )
            )
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val metadata = getFileMetadata(client, url)

        assertEquals(1024L, metadata.size)
        assertTrue(metadata.supportsParallel)
    }

    @Test
    fun `getFileMetadata returns correct metadata when parallel support is NOT present`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentLength to listOf("2048")
                )
            )
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val metadata = getFileMetadata(client, url)

        assertEquals(2048L, metadata.size)
        assertFalse(metadata.supportsParallel)
    }

    @Test
    fun `getFileMetadata returns zero size when Content-Length is missing`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.AcceptRanges to listOf("bytes")
                )
            )
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val metadata = getFileMetadata(client, url)

        assertEquals(0L, metadata.size)
        assertTrue(metadata.supportsParallel)
    }

    @Test
    fun `getFileMetadata throws exception on non-success status`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val exception = assertFailsWith<MetadataFetchException> {
            getFileMetadata(client, url)
        }

        assertEquals(HttpStatusCode.NotFound, exception.statusCode)
    }

    @Test
    fun `getFileMetadata supports parallel when Accept-Ranges is bytes case-insensitive`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentLength to listOf("1024"),
                    HttpHeaders.AcceptRanges to listOf("Bytes")
                )
            )
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val metadata = getFileMetadata(client, url)

        assertTrue(metadata.supportsParallel, "Should support parallel even if 'Bytes' is capitalized")
    }

    @Test
    fun `getFileMetadata returns zero size when Content-Length is malformed`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentLength to listOf("not-a-number")
                )
            )
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val metadata = getFileMetadata(client, url)

        assertEquals(0L, metadata.size)
        assertFalse(metadata.supportsParallel)
    }

    @Test
    fun `getFileMetadata throws ConnectionException when connection fails`() = runBlocking {
        val mockEngine = MockEngine { _ ->
            throw java.io.IOException("Connection failed")
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val exception = assertFailsWith<ConnectionException> {
            getFileMetadata(client, url)
        }

        assertTrue(exception.message!!.contains("Network error or connection dropped for URL"))
        assertEquals("Connection failed", exception.cause?.message)
    }

    @Test
    fun `getFileMetadata throws RequestTimeoutException when request times out`() = runBlocking {
        val mockEngine = MockEngine { _ ->
            throw io.ktor.client.plugins.HttpRequestTimeoutException("http://example.com/file.txt", 1000L, null)
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val exception = assertFailsWith<RequestTimeoutException> {
            getFileMetadata(client, url)
        }

        assertTrue(exception.message!!.contains("Request timed out for URL"))
    }
}
