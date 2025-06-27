# Keycloak M2M Authentication Between Spring Boot Applications

## Overview

This tutorial demonstrates how to set up two Spring Boot applications that communicate securely using Keycloak's Client Credentials flow:
- **Service A**: Acts as a client that calls Service B
- **Service B**: Acts as a resource server that validates tokens from Service A

## Architecture

```
┌─────────────┐         ┌──────────┐         ┌─────────────┐
│  Service A  │────────►│ Keycloak │◄────────│  Service B  │
│   (Client)  │         │  Server  │         │  (Resource) │
└─────────────┘         └──────────┘         └─────────────┘
       │                                              ▲
       │                                              │
       └──────────────────────────────────────────────┘
                    API Call with Token
```

## Keycloak Setup

### 1. Run Keycloak with Docker

```bash
docker run -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

### 2. Configure Keycloak

1. Access Keycloak at http://localhost:8080
2. Login with admin/admin
3. Create a new realm called `microservices`

### 3. Create Clients in Keycloak

#### Service A Client (Client Application):
1. Go to Clients → Create Client
2. Client ID: `service-a`
3. Client Protocol: `openid-connect`
4. Client Authentication: `ON`
5. Authorization: `OFF`
6. Authentication flow: Uncheck all except `Service accounts roles`
7. Save and go to Credentials tab
8. Copy the client secret

#### Service B Client (Resource Server):
1. Create another client with ID: `service-b`
2. Client Protocol: `openid-connect`
3. Client Authentication: `ON`
4. Authorization: `OFF`
5. Authentication flow: Uncheck all except `Service accounts roles`

### 4. Create Roles and Assign Permissions

1. Go to Realm roles → Create role
2. Create role: `service-b-access`
3. Go to Clients → service-a → Service account roles
4. Assign the `service-b-access` role

## Service A Implementation (Client)

### Project Structure
```
service-a/
├── build.gradle.kts
├── src/main/kotlin/com/example/servicea/
│   ├── ServiceAApplication.kt
│   ├── config/
│   │   ├── SecurityConfig.kt
│   │   └── RestClientConfig.kt
│   ├── service/
│   │   ├── OAuth2TokenService.kt
│   │   └── ServiceBClient.kt
│   └── controller/
│       └── ServiceAController.kt
└── src/main/resources/
    └── application.yml
```

### build.gradle.kts

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
```

### application.yml (Service A)

```yaml
server:
  port: 8081

spring:
  application:
    name: service-a
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: service-a
            client-secret: ${KEYCLOAK_CLIENT_SECRET:your-client-secret-here}
            authorization-grant-type: client_credentials
            scope: openid
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/microservices
            token-uri: http://localhost:8080/realms/microservices/protocol/openid-connect/token

service-b:
  base-url: http://localhost:8082

logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.web.client: DEBUG
```

### ServiceAApplication.kt

```kotlin
package com.example.servicea

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServiceAApplication

fun main(args: Array<String>) {
    runApplication<ServiceAApplication>(*args)
}
```

### SecurityConfig.kt (Service A)

```kotlin
package com.example.servicea.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().permitAll() // Service A is just a client
            }

        return http.build()
    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build()

        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientRepository
        )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

        return authorizedClientManager
    }
}
```

### RestClientConfig.kt (Service A)

```kotlin
package com.example.servicea.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class RestClientConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build()
    }
}
```

### OAuth2TokenService.kt

```kotlin
package com.example.servicea.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class OAuth2TokenService(
    private val authorizedClientManager: OAuth2AuthorizedClientManager
) {

    fun getAccessToken(): String {
        val principal = SecurityContextHolder.getContext().authentication?.name ?: "service-a"
        
        val authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId("keycloak")
            .principal(principal)
            .build()

        val authorizedClient: OAuth2AuthorizedClient = 
            authorizedClientManager.authorize(authorizeRequest)
                ?: throw IllegalStateException("Failed to authorize client")

        return authorizedClient.accessToken.tokenValue
    }
}
```

### ServiceBClient.kt

```kotlin
package com.example.servicea.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException

@Service
class ServiceBClient(
    private val restTemplate: RestTemplate,
    private val tokenService: OAuth2TokenService,
    @Value("\${service-b.base-url}") private val serviceBBaseUrl: String
) {

    fun getDataFromServiceB(): ServiceBResponse? {
        val headers = createAuthHeaders()
        val entity = HttpEntity<Any>(headers)

        return try {
            val response = restTemplate.exchange(
                "$serviceBBaseUrl/api/data",
                HttpMethod.GET,
                entity,
                ServiceBResponse::class.java
            )
            response.body
        } catch (e: HttpClientErrorException) {
            println("Error calling Service B: ${e.message}")
            println("Response body: ${e.responseBodyAsString}")
            throw e
        }
    }

    fun sendDataToServiceB(data: ServiceBRequest): ServiceBResponse? {
        val headers = createAuthHeaders()
        val entity = HttpEntity(data, headers)

        return try {
            val response = restTemplate.exchange(
                "$serviceBBaseUrl/api/data",
                HttpMethod.POST,
                entity,
                ServiceBResponse::class.java
            )
            response.body
        } catch (e: HttpClientErrorException) {
            println("Error calling Service B: ${e.message}")
            println("Response body: ${e.responseBodyAsString}")
            throw e
        }
    }

    private fun createAuthHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(tokenService.getAccessToken())
        return headers
    }
}

data class ServiceBRequest(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ServiceBResponse(
    val id: String,
    val message: String,
    val processedAt: Long
)
```

### ServiceAController.kt

```kotlin
package com.example.servicea.controller

import com.example.servicea.service.ServiceBClient
import com.example.servicea.service.ServiceBRequest
import com.example.servicea.service.OAuth2TokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ServiceAController(
    private val serviceBClient: ServiceBClient,
    private val tokenService: OAuth2TokenService
) {

    @GetMapping("/call-service-b")
    fun callServiceB(): ResponseEntity<Any> {
        return try {
            val response = serviceBClient.getDataFromServiceB()
            ResponseEntity.ok(mapOf(
                "source" to "service-a",
                "serviceBResponse" to response,
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "error" to "Failed to call Service B",
                "message" to e.message
            ))
        }
    }

    @PostMapping("/send-to-service-b")
    fun sendToServiceB(@RequestBody message: String): ResponseEntity<Any> {
        return try {
            val request = ServiceBRequest(message = message)
            val response = serviceBClient.sendDataToServiceB(request)
            ResponseEntity.ok(mapOf(
                "source" to "service-a",
                "request" to request,
                "serviceBResponse" to response
            ))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "error" to "Failed to send to Service B",
                "message" to e.message
            ))
        }
    }

    @GetMapping("/token")
    fun getToken(): ResponseEntity<Map<String, String>> {
        return try {
            val token = tokenService.getAccessToken()
            ResponseEntity.ok(mapOf("token" to token))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "error" to "Failed to get token",
                "message" to e.message.orEmpty()
            ))
        }
    }
}
```

## Service B Implementation (Resource Server)

### Project Structure
```
service-b/
├── build.gradle.kts
├── src/main/kotlin/com/example/serviceb/
│   ├── ServiceBApplication.kt
│   ├── config/
│   │   └── SecurityConfig.kt
│   ├── controller/
│   │   └── ServiceBController.kt
│   └── model/
│       └── DataModels.kt
└── src/main/resources/
    └── application.yml
```

### build.gradle.kts (Service B)

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
```

### application.yml (Service B)

```yaml
server:
  port: 8082

spring:
  application:
    name: service-b
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/microservices
          jwk-set-uri: http://localhost:8080/realms/microservices/protocol/openid-connect/certs

logging:
  level:
    org.springframework.security: DEBUG
```

### SecurityConfig.kt (Service B)

```kotlin
package com.example.serviceb.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/**").hasAuthority("ROLE_service-b-access")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter())
        return jwtConverter
    }

    @Bean
    fun jwtGrantedAuthoritiesConverter(): Converter<Jwt, Collection<GrantedAuthority>> {
        return Converter { jwt ->
            val authorities = mutableListOf<GrantedAuthority>()

            // Extract realm roles
            val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
            if (realmAccess != null) {
                val roles = realmAccess["roles"] as? List<*>
                roles?.forEach { role ->
                    authorities.add(SimpleGrantedAuthority("ROLE_$role"))
                }
            }

            // Extract resource roles
            val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access")
            if (resourceAccess != null) {
                resourceAccess.forEach { (client, access) ->
                    val clientAccess = access as? Map<*, *>
                    val clientRoles = clientAccess?.get("roles") as? List<*>
                    clientRoles?.forEach { role ->
                        authorities.add(SimpleGrantedAuthority("ROLE_${client}_$role"))
                    }
                }
            }

            authorities
        }
    }
}
```

### ServiceBController.kt

```kotlin
package com.example.serviceb.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class ServiceBController {

    @GetMapping("/data")
    fun getData(@AuthenticationPrincipal jwt: Jwt): ServiceBResponse {
        println("Request received from client: ${jwt.subject}")
        println("Client ID: ${jwt.getClaim<String>("azp")}")
        
        return ServiceBResponse(
            id = UUID.randomUUID().toString(),
            message = "Hello from Service B! You are authenticated as: ${jwt.getClaim<String>("azp")}",
            processedAt = System.currentTimeMillis()
        )
    }

    @PostMapping("/data")
    fun processData(
        @RequestBody request: ServiceBRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ServiceBResponse {
        println("Processing request from: ${jwt.getClaim<String>("azp")}")
        println("Request message: ${request.message}")
        
        return ServiceBResponse(
            id = UUID.randomUUID().toString(),
            message = "Processed: ${request.message}",
            processedAt = System.currentTimeMillis()
        )
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP", "service" to "service-b")
    }
}

data class ServiceBRequest(
    val message: String,
    val timestamp: Long
)

data class ServiceBResponse(
    val id: String,
    val message: String,
    val processedAt: Long
)
```

## Alternative: Using RestClient (Spring 6.1+)

If you're using Spring Boot 3.2+ with Spring Framework 6.1+, you can use the new `RestClient` instead of `RestTemplate`:

### RestClientConfig.kt (Alternative)

```kotlin
package com.example.servicea.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig {

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .defaultHeader("Accept", "application/json")
            .build()
    }
}
```

### ServiceBClient.kt (Using RestClient)

```kotlin
package com.example.servicea.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ServiceBClient(
    private val restClient: RestClient,
    private val tokenService: OAuth2TokenService,
    @Value("\${service-b.base-url}") private val serviceBBaseUrl: String
) {

    fun getDataFromServiceB(): ServiceBResponse? {
        return restClient.get()
            .uri("$serviceBBaseUrl/api/data")
            .header("Authorization", "Bearer ${tokenService.getAccessToken()}")
            .retrieve()
            .body(ServiceBResponse::class.java)
    }

    fun sendDataToServiceB(data: ServiceBRequest): ServiceBResponse? {
        return restClient.post()
            .uri("$serviceBBaseUrl/api/data")
            .header("Authorization", "Bearer ${tokenService.getAccessToken()}")
            .contentType(MediaType.APPLICATION_JSON)
            .body(data)
            .retrieve()
            .body(ServiceBResponse::class.java)
    }
}
```

## Testing the Setup

### 1. Start Keycloak
```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

### 2. Configure Keycloak
Follow the Keycloak setup steps above to create realm, clients, and roles.

### 3. Update Client Secrets
Update the `KEYCLOAK_CLIENT_SECRET` in Service A's application.yml with the actual client secret from Keycloak.

### 4. Start Both Services
```bash
# Terminal 1 - Start Service B
cd service-b
./gradlew bootRun

# Terminal 2 - Start Service A
cd service-a
./gradlew bootRun
```

### 5. Test the Communication

```bash
# Test Service A calling Service B
curl http://localhost:8081/api/call-service-b

# Expected response:
{
  "source": "service-a",
  "serviceBResponse": {
    "id": "some-uuid",
    "message": "Hello from Service B! You are authenticated as: service-a",
    "processedAt": 1234567890
  },
  "timestamp": 1234567890
}

# Test sending data from Service A to Service B
curl -X POST http://localhost:8081/api/send-to-service-b \
  -H "Content-Type: application/json" \
  -d '"Hello from Service A"'

# Get the access token (for debugging)
curl http://localhost:8081/api/token
```

## Adding Request/Response Interceptors

For better debugging and monitoring, add interceptors:

### LoggingInterceptor.kt

```kotlin
package com.example.servicea.config

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Component
class LoggingInterceptor : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        logResponse(response)
        return response
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        println("===========================request begin================================================")
        println("URI         : ${request.uri}")
        println("Method      : ${request.method}")
        println("Headers     : ${request.headers}")
        println("Request body: ${String(body, StandardCharsets.UTF_8)}")
        println("==========================request end================================================")
    }

    private fun logResponse(response: ClientHttpResponse) {
        println("============================response begin==========================================")
        println("Status code  : ${response.statusCode}")
        println("Status text  : ${response.statusText}")
        println("Headers      : ${response.headers}")
        println("=======================response end=================================================")
    }
}
```

Then update RestClientConfig to use the interceptor:

```kotlin
@Bean
fun restTemplate(
    builder: RestTemplateBuilder,
    loggingInterceptor: LoggingInterceptor
): RestTemplate {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(10))
        .interceptors(loggingInterceptor)
        .build()
}
```

## Production Considerations

### 1. Use HTTPS
```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          keycloak:
            issuer-uri: https://keycloak.yourdomain.com/realms/microservices
```

### 2. Connection Pooling
```kotlin
@Bean
fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
    val factory = HttpComponentsClientHttpRequestFactory()
    factory.setConnectTimeout(5000)
    factory.setConnectionRequestTimeout(5000)
    
    return builder
        .requestFactory { factory }
        .build()
}
```

### 3. Retry Logic
```kotlin
@Bean
fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(10))
        .additionalInterceptors(RetryInterceptor())
        .build()
}
```

### 4. Health Checks
```kotlin
@Component
class KeycloakHealthIndicator(
    @Value("\${spring.security.oauth2.client.provider.keycloak.issuer-uri}") 
    private val issuerUri: String,
    private val restTemplate: RestTemplate
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            restTemplate.getForObject(
                "$issuerUri/.well-known/openid-configuration", 
                String::class.java
            )
            Health.up().build()
        } catch (ex: Exception) {
            Health.down().withException(ex).build()
        }
    }
}
```

## Troubleshooting

### Common Issues:

1. **401 Unauthorized**: Check client credentials and role assignments
2. **Connection refused**: Ensure Keycloak is running and accessible
3. **Invalid token**: Verify issuer-uri matches in both services
4. **No authorities**: Check the JWT converter is extracting roles correctly

### Debug JWT Content
Use jwt.io to decode and inspect your access tokens.

## Summary

This example demonstrates:
- Service A obtaining tokens from Keycloak using Client Credentials
- Service A calling Service B with RestTemplate/RestClient (no WebFlux)
- Service B validating tokens and checking roles
- Proper security configuration for both client and resource server
- Keycloak-specific JWT claim extraction for roles

The setup ensures secure M2M communication with proper authentication and authorization using industry-standard OAuth2/OIDC protocols.