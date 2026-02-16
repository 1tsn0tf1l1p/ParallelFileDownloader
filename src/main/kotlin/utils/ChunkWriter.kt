package utils

import exceptions.FileWriteException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class ChunkWriter(filePath: Path) : AutoCloseable {

    private val channel: FileChannel = try {
        FileChannel.open(
            filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        )
    } catch (e: IOException) {
        throw FileWriteException("Failed to open file for writing: $filePath", e)
    }

    fun writeChunk(startByte: Long, data: ByteArray) {
        val buffer = ByteBuffer.wrap(data)
        var currentPosition = startByte

        try {
            while (buffer.hasRemaining()) {
                val written = channel.write(buffer, currentPosition)
                if (written == 0) {
                    throw FileWriteException("Failed to write data to file at position $currentPosition (disk full or other I/O issue)")
                }
                currentPosition += written
            }
        } catch (e: IOException) {
            if (e is FileWriteException) throw e
            throw FileWriteException("Error writing chunk at position $startByte", e)
        }
    }

    fun setLength(totalSize: Long) {
        if (totalSize < 0) return

        try {
            if (channel.size() < totalSize) {
                val singleByte = ByteBuffer.allocate(1)
                channel.write(singleByte, totalSize - 1)
            }
            channel.truncate(totalSize)
        } catch (e: IOException) {
            throw FileWriteException("Error setting file length to $totalSize", e)
        }
    }

    override fun close() {
        if (channel.isOpen) {
            channel.close()
        }
    }
}