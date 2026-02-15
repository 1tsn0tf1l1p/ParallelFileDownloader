package exceptions

class ConnectionException(message: String, cause: Throwable) :
    NetworkFetchException(message, cause)
