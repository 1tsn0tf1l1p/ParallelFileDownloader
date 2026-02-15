import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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
    fun `getFileMetadata throws NetworkFetchException when connection fails`() = runBlocking {
        val mockEngine = MockEngine { _ ->
            throw Exception("Connection failed")
        }
        val client = HttpClient(mockEngine)
        val url = "http://example.com/file.txt"

        val exception = assertFailsWith<NetworkFetchException> {
            getFileMetadata(client, url)
        }

        assertTrue(exception.message!!.contains("Failed to connect to the provided URL"))
        assertEquals("Connection failed", exception.cause?.message)
    }
}
