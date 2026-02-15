package exceptions

class ConnectionException(url: String, cause: Throwable) :
    NetworkFetchException("Network error or connection dropped for URL: $url", cause)
