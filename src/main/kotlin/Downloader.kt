import exceptions.ChunkDownloadException
import exceptions.ConnectionException
import exceptions.RequestTimeoutException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import model.Chunk
import java.io.IOException

suspend fun downloadChunk(client: HttpClient, url: String, chunk: Chunk): ByteArray {
    try {
        val response: HttpResponse = client.get(url) {
            if (chunk.endByte != -1L) {
                header(HttpHeaders.Range, "bytes=${chunk.startByte}-${chunk.endByte}")
            }
        }

        if (!response.status.isSuccess()) {
            throw ChunkDownloadException(chunk.id, response.status)
        }

        return response.readRawBytes()
    } catch (e: HttpRequestTimeoutException) {
        throw RequestTimeoutException("Chunk ${chunk.id} timed out for URL: $url", e)
    } catch (e: IOException) {
        throw ConnectionException("Chunk ${chunk.id} connection dropped for URL: $url", e)
    }
}