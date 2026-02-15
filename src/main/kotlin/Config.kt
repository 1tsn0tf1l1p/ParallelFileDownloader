import io.ktor.client.*
import io.ktor.client.engine.cio.*

object Config {
    val url: String = "http://localhost:8081/my-test-file.txt"

    const val MIN_CHUNK_SIZE: Long = 1024 * 1024
    const val MAX_WORKERS: Int = 8

    fun createClient(): HttpClient = HttpClient(CIO)
}