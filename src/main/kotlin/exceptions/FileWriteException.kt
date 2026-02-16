package exceptions

import java.io.IOException

class FileWriteException(message: String, cause: Throwable? = null) : IOException(message, cause)
