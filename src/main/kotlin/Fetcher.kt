import exceptions.ConnectionException
import exceptions.MetadataFetchException
import exceptions.RequestTimeoutException
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.FileMetadata
import java.io.IOException

suspend fun getFileMetadata(): FileMetadata {
    val client = Config.client
    val url = Config.url
    val response: HttpResponse = try {
        client.head(url)
    } catch (e: HttpRequestTimeoutException) {
        throw RequestTimeoutException("Request timed out for URL: $url", e)
    } catch (e: IOException) {
        throw ConnectionException("Network error or connection dropped for URL: $url", e)
    }

    if (!response.status.isSuccess()) {
        throw MetadataFetchException(response.status)
    }

    val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
    val acceptRanges = response.headers[HttpHeaders.AcceptRanges]
    val isParallelismSupported = acceptRanges?.equals("bytes", ignoreCase = true) == true

    return FileMetadata(contentLength, isParallelismSupported)
}