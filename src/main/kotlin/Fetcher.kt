import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

suspend fun getFileMetadata(client: HttpClient, url: String): FileMetadata {
    val response: HttpResponse = try {
        client.head(url)
    } catch (e: Exception) {
        throw NetworkFetchException("Failed to connect to the provided URL: $url", e)
    }

    if (!response.status.isSuccess()) {
        throw MetadataFetchException(response.status)
    }

    val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
    val acceptRanges = response.headers[HttpHeaders.AcceptRanges]
    val isParallelismSupported = acceptRanges?.equals("bytes", ignoreCase = true) == true

    return FileMetadata(contentLength, isParallelismSupported)
}