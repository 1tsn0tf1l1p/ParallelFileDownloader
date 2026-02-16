package exceptions

import io.ktor.http.*

class ChunkDownloadException(chunkId: Int, statusCode: HttpStatusCode) :
    Exception("Failed to download chunk $chunkId. HTTP Status: $statusCode")
