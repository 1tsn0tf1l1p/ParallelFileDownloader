package exceptions

class RequestTimeoutException(message: String, cause: Throwable) :
    NetworkFetchException(message, cause)
