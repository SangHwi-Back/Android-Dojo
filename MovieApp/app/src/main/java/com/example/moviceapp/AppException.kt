package com.example.moviceapp

/**
 * Base sealed class for all application-specific exceptions
 * Provides a consistent way to handle errors throughout the app
 */
sealed class AppException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Network-related exceptions
     */
    sealed class NetworkException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class NoConnection(cause: Throwable? = null) : NetworkException("No internet connection available", cause)
        class Timeout(cause: Throwable? = null) : NetworkException("Request timed out", cause)
        class ServerError(val statusCode: Int, cause: Throwable? = null) : NetworkException("Server error: $statusCode", cause)
        class Unknown(cause: Throwable? = null) : NetworkException("Unknown network error", cause)
    }

    /**
     * Authentication and authorization exceptions
     */
    sealed class AuthException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class NotAuthenticated(cause: Throwable? = null) : AuthException("User is not authenticated", cause)
        class TokenExpired(cause: Throwable? = null) : AuthException("Authentication token has expired", cause)
        class InvalidCredentials(cause: Throwable? = null) : AuthException("Invalid email or password", cause)
        class Unauthorized(cause: Throwable? = null) : AuthException("You don't have permission to access this resource", cause)
    }

    /**
     * Booking-related exceptions
     */
    sealed class BookingException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class NoSeatsAvailable(val movieTitle: String? = null) : BookingException(
            movieTitle?.let { "No seats available for $it" } ?: "No seats available"
        )
        class BookingNotFound(val bookingId: Int? = null) : BookingException(
            bookingId?.let { "Booking #$it not found" } ?: "Booking not found"
        )
        class InvalidShowtime(cause: Throwable? = null) : BookingException("Selected showtime is no longer available", cause)
        class PaymentFailed(val reason: String? = null, cause: Throwable? = null) : BookingException(
            reason?.let { "Payment failed: $it" } ?: "Payment failed", cause
        )
        class BookingAlreadyExists(cause: Throwable? = null) : BookingException("You already have a booking for this showtime", cause)
    }

    /**
     * Payment method exceptions
     */
    sealed class PaymentMethodException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class NotFound(val cardId: Int? = null) : PaymentMethodException(
            cardId?.let { "Payment method #$it not found" } ?: "Payment method not found"
        )
        class InvalidCard(val reason: String? = null) : PaymentMethodException(
            reason?.let { "Invalid card: $it" } ?: "Invalid card details"
        )
        class NoDefaultCard(cause: Throwable? = null) : PaymentMethodException("No default payment method set", cause)
        class CardExpired(cause: Throwable? = null) : PaymentMethodException("Payment card has expired", cause)
    }

    /**
     * User account exceptions
     */
    sealed class UserException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class NotFound(cause: Throwable? = null) : UserException("User not found", cause)
        class InvalidProfile(val field: String? = null) : UserException(
            field?.let { "Invalid $it" } ?: "Invalid profile data"
        )
        class UpdateFailed(cause: Throwable? = null) : UserException("Failed to update user profile", cause)
    }

    /**
     * Movie and theater exceptions
     */
    sealed class MovieException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class NotFound(val movieId: Int? = null) : MovieException(
            movieId?.let { "Movie #$it not found" } ?: "Movie not found"
        )
        class NoShowtimes(val movieTitle: String? = null) : MovieException(
            movieTitle?.let { "No showtimes available for $it" } ?: "No showtimes available"
        )
    }

    /**
     * Data validation exceptions
     */
    sealed class ValidationException(message: String? = null, cause: Throwable? = null) : AppException(message, cause) {
        class InvalidEmail(cause: Throwable? = null) : ValidationException("Invalid email format", cause)
        class InvalidPhoneNumber(cause: Throwable? = null) : ValidationException("Invalid phone number format", cause)
        class InvalidInput(val field: String? = null) : ValidationException(
            field?.let { "Invalid input for $it" } ?: "Invalid input"
        )
        class MissingRequiredField(val field: String) : ValidationException("$field is required")
    }

    /**
     * Generic application exceptions
     */
    class Unknown(message: String? = "An unknown error occurred", cause: Throwable? = null) : AppException(message, cause)
    class NotImplemented(feature: String? = null) : AppException(
        feature?.let { "$it is not yet implemented" } ?: "Feature not yet implemented"
    )
}

/**
 * Extension function to convert generic exceptions to AppException
 */
fun Throwable.toAppException(): AppException {
    return when (this) {
        is AppException -> this
        is java.net.UnknownHostException -> AppException.NetworkException.NoConnection(this)
        is java.net.SocketTimeoutException -> AppException.NetworkException.Timeout(this)
        is java.io.IOException -> AppException.NetworkException.Unknown(this)
        else -> AppException.Unknown(this.message, this)
    }
}

/**
 * Extension function to get user-friendly error message
 */
fun AppException.getUserMessage(): String {
    return this.message ?: "An error occurred"
}