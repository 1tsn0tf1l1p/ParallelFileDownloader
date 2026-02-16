package exceptions

import io.ktor.http.HttpStatusCode

class ChunkDownloadException(chunkId: Int, statusCode: HttpStatusCode) :
    Exception("Failed to download chunk $chunkId. HTTP Status: $statusCode")
