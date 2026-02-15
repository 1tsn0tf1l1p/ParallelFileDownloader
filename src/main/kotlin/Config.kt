import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

object Config {
    val url: String = "http://localhost:8081/my-test-file.txt"

    fun createClient(): HttpClient = HttpClient(CIO)
}