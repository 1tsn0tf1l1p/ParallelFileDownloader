package utils

import io.ktor.client.*
import kotlinx.coroutines.*
import java.nio.file.Path

class DownloadManager(private val client: HttpClient) {

    suspend fun download(url: String, destinationPath: Path) = withContext(Dispatchers.IO) {
        val metadata = getFileMetadata()
        val chunks = ChunkCalculator.calculate(metadata)

        ChunkWriter(destinationPath).use { writer ->
            writer.setLength(metadata.size)

            coroutineScope {
                val jobs = chunks.map { chunk ->
                    async {
                        val data = downloadChunk(client, url, chunk)
                        writer.writeChunk(chunk.startByte, data)
                    }
                }

                jobs.awaitAll()
            }
        }

        println("Download completed: $destinationPath")
    }
}