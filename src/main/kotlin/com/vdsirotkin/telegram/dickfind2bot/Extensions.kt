package com.vdsirotkin.telegram.dickfind2bot

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.io.IOException
import kotlin.coroutines.resume

suspend fun <T : BaseRequest<T, R>?, R : BaseResponse?> DickfindBot.executeAsync(request: T): R {
    return suspendCancellableCoroutine {
        execute(request, object : Callback<T, R> {
            override fun onResponse(request: T, response: R) {
                it.resume(response)
            }

            override fun onFailure(request: T, error: IOException?) {
                it.cancel(error)
            }
        })
    }
}

private val context = CoroutineExceptionHandler { _, t ->
    LoggerFactory.getLogger("com.vdsirotkin.telegram.dickfind2bot.CoroutineExceptionHandler").error(t.message, t)
}

object ExceptionLoggingContext : CoroutineExceptionHandler by context