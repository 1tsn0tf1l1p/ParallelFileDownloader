import model.FileMetadata
import kotlin.test.Test
import kotlin.test.assertEquals

class ChunkCalculatorTest {

    @Test
    fun `calculate returns single chunk for small file`() {
        val totalSize = Config.MIN_CHUNK_SIZE / 2
        val chunks = ChunkCalculator.calculate(FileMetadata(totalSize, true))

        assertEquals(1, chunks.size)
        assertEquals(0L, chunks[0].startByte)
        assertEquals(totalSize - 1, chunks[0].endByte)
    }

    @Test
    fun `calculate respects MAX_WORKERS`() {
        val totalSize = Config.MIN_CHUNK_SIZE * (Config.MAX_WORKERS + 5)
        val chunks = ChunkCalculator.calculate(FileMetadata(totalSize, true))

        assertEquals(Config.MAX_WORKERS, chunks.size)
    }

    @Test
    fun `calculate covers entire file size`() {
        val totalSize = 12345678L
        val chunks = ChunkCalculator.calculate(FileMetadata(totalSize, true))

        assertEquals(0L, chunks.first().startByte)
        assertEquals(totalSize - 1, chunks.last().endByte)

        for (i in 0 until chunks.size - 1) {
            assertEquals(chunks[i].endByte + 1, chunks[i + 1].startByte)
        }
    }

    @Test
    fun `calculate handles exactly MIN_CHUNK_SIZE`() {
        val totalSize = Config.MIN_CHUNK_SIZE
        val chunks = ChunkCalculator.calculate(FileMetadata(totalSize, true))

        assertEquals(1, chunks.size)
    }

    @Test
    fun `calculate returns single chunk when parallel not supported`() {
        val totalSize = Config.MIN_CHUNK_SIZE * 10
        val metadata = FileMetadata(totalSize, false)
        val chunks = ChunkCalculator.calculate(metadata)

        assertEquals(1, chunks.size)
        assertEquals(0, chunks[0].id)
        assertEquals(0L, chunks[0].startByte)
        assertEquals(totalSize - 1, chunks[0].endByte)
    }

    @Test
    fun `calculate handles zero size`() {
        val metadata = FileMetadata(0L, true)
        val chunks = ChunkCalculator.calculate(metadata)

        assertEquals(1, chunks.size)
        assertEquals(0, chunks[0].id)
        assertEquals(0L, chunks[0].startByte)
        assertEquals(-1L, chunks[0].endByte)
    }

    @Test
    fun `calculate handles negative size`() {
        val metadata = FileMetadata(-10L, true)
        val chunks = ChunkCalculator.calculate(metadata)

        assertEquals(1, chunks.size)
        assertEquals(0, chunks[0].id)
        assertEquals(0L, chunks[0].startByte)
        assertEquals(-11L, chunks[0].endByte)
    }
}
