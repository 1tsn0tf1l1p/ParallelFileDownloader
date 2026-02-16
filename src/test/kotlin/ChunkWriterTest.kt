import exceptions.FileWriteException
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ChunkWriterTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `writeChunk writes data at correct position`() {
        val filePath = tempDir.resolve("testfile")
        val chunkWriter = ChunkWriter(filePath)

        val data1 = "abc".toByteArray()
        val data2 = "def".toByteArray()

        chunkWriter.use {
            it.writeChunk(0, data1)
            it.writeChunk(3, data2)
        }

        val writtenData = Files.readAllBytes(filePath)
        assertArrayEquals("abcdef".toByteArray(), writtenData)
    }

    @Test
    fun `writeChunk writes data out of order`() {
        val filePath = tempDir.resolve("testfile_out_of_order")
        val chunkWriter = ChunkWriter(filePath)

        val data1 = "abc".toByteArray()
        val data2 = "def".toByteArray()

        chunkWriter.use {
            it.writeChunk(3, data2)
            it.writeChunk(0, data1)
        }

        val writtenData = Files.readAllBytes(filePath)
        assertArrayEquals("abcdef".toByteArray(), writtenData)
    }

    @Test
    fun `writeChunk overwrites data`() {
        val filePath = tempDir.resolve("testfile_overwrite")
        val chunkWriter = ChunkWriter(filePath)

        val data1 = "abcde".toByteArray()
        val data2 = "xyz".toByteArray()

        chunkWriter.use {
            it.writeChunk(0, data1)
            it.writeChunk(1, data2)
        }

        val writtenData = Files.readAllBytes(filePath)
        assertArrayEquals("axyze".toByteArray(), writtenData)
    }

    @Test
    fun `setLength pre-allocates file`() {
        val filePath = tempDir.resolve("testfile_preallocate")
        val chunkWriter = ChunkWriter(filePath)

        chunkWriter.use {
            it.setLength(100)
        }

        assertEquals(100L, Files.size(filePath))
    }

    @Test
    fun `setLength truncates file`() {
        val filePath = tempDir.resolve("testfile_truncate")
        Files.write(filePath, ByteArray(200))
        val chunkWriter = ChunkWriter(filePath)

        chunkWriter.use {
            it.setLength(50)
        }

        assertEquals(50L, Files.size(filePath))
    }

    @Test
    fun `writeChunk throws FileWriteException when channel is closed`() {
        val filePath = tempDir.resolve("testfile_closed")
        val chunkWriter = ChunkWriter(filePath)
        chunkWriter.close()

        assertThrows<FileWriteException> {
            chunkWriter.writeChunk(0, "data".toByteArray())
        }
    }

    @Test
    fun `setLength throws FileWriteException when channel is closed`() {
        val filePath = tempDir.resolve("testfile_setLength_closed")
        val chunkWriter = ChunkWriter(filePath)
        chunkWriter.close()

        assertThrows<FileWriteException> {
            chunkWriter.setLength(100)
        }
    }

    @Test
    fun `constructor throws FileWriteException when file cannot be opened`() {
        assertThrows<FileWriteException> {
            ChunkWriter(tempDir)
        }
    }
}
