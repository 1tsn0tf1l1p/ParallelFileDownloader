package utils

import io.ktor.client.*
import kotlinx.coroutines.*
import java.nio.file.Path
import org.slf4j.LoggerFactory

class DownloadManager(private val client: HttpClient) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun download(url: String, destinationPath: Path) = withContext(Dispatchers.IO) {
        logger.debug("Fetching metadata for {}", url)
        val metadata = getFileMetadata()
        
        logger.info("File size: {} bytes, Parallel support: {}", metadata.size, metadata.supportsParallel)
        val chunks = ChunkCalculator.calculate(metadata)
        logger.info("Splitting into {} chunks", chunks.size)

        ChunkWriter(destinationPath).use { writer ->
            logger.debug("Pre-allocating {} bytes on disk", metadata.size)
            writer.setLength(metadata.size)

            coroutineScope {
                chunks.map { chunk ->
                    async {
                        logger.debug("Starting chunk {}: range {}-{}", chunk.id, chunk.startByte, chunk.endByte)
                        val data = downloadChunk(client, url, chunk)
                        writer.writeChunk(chunk.startByte, data)
                        logger.debug("Chunk {} completed and written to disk", chunk.id)
                    }
                }.awaitAll()
            }
        }
    }
}