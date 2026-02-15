import exceptions.ConnectionException
import exceptions.MetadataFetchException
import exceptions.RequestTimeoutException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import java.io.IOException

suspend fun getFileMetadata(client: HttpClient, url: String): FileMetadata {
    val response: HttpResponse = try {
        client.head(url)
    } catch (e: HttpRequestTimeoutException) {
        throw RequestTimeoutException(url, e)
    } catch (e: IOException) {
        throw ConnectionException(url, e)
    }

    if (!response.status.isSuccess()) {
        throw MetadataFetchException(response.status)
    }

    val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
    val acceptRanges = response.headers[HttpHeaders.AcceptRanges]
    val isParallelismSupported = acceptRanges?.equals("bytes", ignoreCase = true) == true

    return FileMetadata(contentLength, isParallelismSupported)
}