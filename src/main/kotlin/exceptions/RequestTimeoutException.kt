package exceptions

class RequestTimeoutException(url: String, cause: Throwable) :
    NetworkFetchException("Request timed out for URL: $url", cause)
