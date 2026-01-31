package com.debdut.simpletemplate.product.data

import io.ktor.client.HttpClient

/**
 * Platform-specific HTTP client creation (engine differs per target).
 * Actual implementations use OkHttp (Android), CIO (JVM), Js (wasmJs), Darwin (iOS).
 */
expect fun createHttpClient(): HttpClient
