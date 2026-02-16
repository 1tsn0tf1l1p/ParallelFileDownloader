package utils

import exceptions.ChunkDownloadException
import exceptions.ConnectionException
import exceptions.RequestTimeoutException
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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