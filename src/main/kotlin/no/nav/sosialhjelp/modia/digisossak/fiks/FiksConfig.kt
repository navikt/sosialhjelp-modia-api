package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.util.Timeout
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.util.concurrent.TimeUnit

@Configuration
class FiksConfig(
    private val restClientBuilder: RestClient.Builder,
    private val clientProperties: ClientProperties,
    @Value("\${retry_fiks_max_attempts}") private val maxAttempts: Int,
    @Value("\${retry_fiks_initial_delay}") private val initialDelay: Long,
) {
    @Bean
    fun fiksRestClient(): RestClient {
        val connectionManager =
            PoolingHttpClientConnectionManager().apply {
                maxTotal = 100
                defaultMaxPerRoute = 100
                setDefaultConnectionConfig(
                    ConnectionConfig
                        .custom()
                        .setConnectTimeout(Timeout.of(30, TimeUnit.SECONDS))
                        .setSocketTimeout(Timeout.of(30, TimeUnit.SECONDS))
                        .build(),
                )
            }

        val requestConfig =
            RequestConfig
                .custom()
                .setResponseTimeout(Timeout.of(2, TimeUnit.MINUTES))
                .setConnectionRequestTimeout(Timeout.of(30, TimeUnit.SECONDS))
                .build()

        val httpClient =
            HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

        return restClientBuilder
            .clone()
            .requestFactory(requestFactory)
            .baseUrl(clientProperties.fiksDigisosEndpointUrl)
            .build()
    }

    @Bean
    fun fiksRetryTemplate(): RetryTemplate {
        val retryPolicy = SimpleRetryPolicy(maxAttempts, mapOf(HttpServerErrorException::class.java to true), true)

        val backOffPolicy =
            ExponentialBackOffPolicy().apply {
                initialInterval = initialDelay
                multiplier = 2.0
                maxInterval = 10000L
            }

        return RetryTemplate().apply {
            setRetryPolicy(retryPolicy)
            setBackOffPolicy(backOffPolicy)
        }
    }
}
