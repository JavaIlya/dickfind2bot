package com.vdsirotkin.telegram.dickfind2bot.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import com.vdsirotkin.telegram.dickfind2bot.DickfindBot
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import java.io.IOException
import java.time.Duration
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

private val retry = Retry.of("telegram_api", RetryConfig.custom<Any>()
    .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(1).toMillis(), 2.0))
    .retryOnException {
        when (it) {
            is TelegramException -> it.response().errorCode() == 429
            else -> true
        }
    }
    .maxAttempts(5)
    .build()
)

private val rateLimiter = RateLimiter.of("telegram_api", RateLimiterConfig.custom()
    .limitForPeriod(25)
    .limitRefreshPeriod(Duration.ofSeconds(2))
    .timeoutDuration(Duration.ofHours(3))
    .build())

private val logger = LoggerFactory.getLogger("telegram.calls")
private val objectMapper = jacksonObjectMapper()

fun <T : BaseRequest<T, R>, R : BaseResponse> TelegramBot.executeSafe(method: T): R {
    return retry.executeCallable {
        rateLimiter.executeCallable {
            logger.info("Request: ${objectMapper.writeValueAsString(method)}")
            val resp = execute(method)
            logger.info("Response: ${objectMapper.writeValueAsString(resp)}")
            resp
        }
    }
}
