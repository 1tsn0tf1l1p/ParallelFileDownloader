package utils

import config.Config
import model.Chunk
import model.FileMetadata

object ChunkCalculator {

    fun calculate(metadata: FileMetadata): List<Chunk> {
        if (!metadata.supportsParallel || metadata.size <= 0L) {
            return listOf(Chunk(id = 0, startByte = 0L, endByte = metadata.size - 1))
        }

        val workerCount = calculateWorkerCount(metadata.size)
        return generateChunks(metadata.size, workerCount)
    }

    private fun calculateWorkerCount(totalSize: Long): Int {
        val desiredWorkers = totalSize / Config.MIN_CHUNK_SIZE
        return desiredWorkers.coerceIn(1L, Config.MAX_WORKERS.toLong()).toInt()
    }

    private fun generateChunks(totalSize: Long, workerCount: Int): List<Chunk> {
        val chunkSize = totalSize / workerCount

        return List(workerCount) { i ->
            val startByte = i * chunkSize
            val endByte = if (i == workerCount - 1) {
                totalSize - 1
            } else {
                startByte + chunkSize - 1
            }
            Chunk(id = i, startByte = startByte, endByte = endByte)
        }
    }
}