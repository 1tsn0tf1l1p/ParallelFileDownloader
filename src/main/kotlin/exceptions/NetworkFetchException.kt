package exceptions

open class NetworkFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)
