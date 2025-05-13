package no.uio.ifi.in2000.team46.data.repository

// result is a sealed class used to wrap success and error outcomes from repository calls
// allows consistent error handling and result propagation throughout the app

sealed class Result<out T> {
    // represents a successful result with data of type T
    data class Success<out T>(val data: T) : Result<T>()

    // represents a failure with an exception
    data class Error(val exception: Throwable) : Result<Nothing>()
}
