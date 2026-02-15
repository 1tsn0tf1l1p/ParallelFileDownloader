package exceptions

import io.ktor.http.HttpStatusCode

class MetadataFetchException(val statusCode: HttpStatusCode) : Exception("File not found or server error: $statusCode")
