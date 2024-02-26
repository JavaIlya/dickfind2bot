package com.vdsirotkin.telegram.dickfind2bot.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramException
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.GetChatMemberResponse
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
    .retryOnResult {
        when (it) {
            is GetChatMemberResponse -> false
            is BaseResponse -> !it.isOk
            else -> false
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
private val objectMapper = ObjectMapper()
    .registerKotlinModule()
    .enable(
        DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE,
        DeserializationFeature.READ_ENUMS_USING_TO_STRING,
        DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
        DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
    )
    .enable(
        MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
    )
    .enable(
        SerializationFeature.WRITE_ENUMS_USING_TO_STRING
    )
    .disable(
        SerializationFeature.FAIL_ON_EMPTY_BEANS,
        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
    )
    .disable(
        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
    )
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)

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

fun User?.trueFirstName() = this?.firstName().let {
    if (it == "Oleksandr") "Oleg" else it
}.orEmpty()
