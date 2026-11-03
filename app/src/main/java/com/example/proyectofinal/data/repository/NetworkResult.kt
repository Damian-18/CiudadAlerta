package com.example.proyectofinal.data.repository
import kotlinx.coroutines.CancellationException
import retrofit2.Response
suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try { Result.success(block()) } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
fun <T> Response<T>.requireBody(): T {
    check(isSuccessful) { errorBody()?.string() ?: "Error HTTP ${code()}" }
    return checkNotNull(body()) { "Respuesta vacía" }
}
