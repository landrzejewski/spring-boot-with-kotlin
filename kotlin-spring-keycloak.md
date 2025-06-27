# Kotlin Spring Boot + Keycloak Resource Server Tutorial

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Setting up Keycloak](#setting-up-keycloak)
4. [Creating the Spring Boot Project](#creating-the-spring-boot-project)
5. [Configuring Spring Security with Keycloak](#configuring-spring-security-with-keycloak)
6. [Creating Protected Resources](#creating-protected-resources)
7. [Testing the Application](#testing-the-application)
8. [Advanced Configuration](#advanced-configuration)

## Overview

This tutorial will guide you through creating a Kotlin Spring Boot application that acts as a resource server protected by Keycloak. We'll implement OAuth 2.0/OpenID Connect authentication and authorization using Spring Security's OAuth2 Resource Server support.

### Architecture Overview
- **Keycloak**: Acts as the Authorization Server (Identity Provider)
- **Spring Boot Application**: Acts as the Resource Server
- **Client**: Any application that obtains tokens from Keycloak to access protected resources

## Prerequisites

- JDK 17 or later
- Kotlin 1.8+
- Maven or Gradle
- Docker (for running Keycloak)
- IDE (IntelliJ IDEA recommended)
- Basic knowledge of Spring Boot and OAuth 2.0

## Setting up Keycloak

### 1. Start Keycloak using Docker

```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

### 2. Configure Keycloak

1. Access Keycloak at `http://localhost:8080`
2. Login with admin/admin
3. Create a new realm:
   - Click on master dropdown → Create Realm
   - Name: `myrealm`
   - Click Create

### 3. Create a Client

1. Navigate to Clients → Create client
2. Configure the client:
   - Client ID: `spring-boot-app`
   - Client Protocol: `openid-connect`
   - Click Next
3. Client Authentication: ON
4. Authorization: OFF
5. Click Next
6. Valid Redirect URIs: `http://localhost:8081/*`
7. Click Save

### 4. Get Client Credentials

1. Go to Clients → spring-boot-app → Credentials
2. Copy the Client Secret (you'll need this later)

### 5. Create a Test User

1. Navigate to Users → Add user
2. Username: `testuser`
3. Email: `test@example.com`
4. Click Create
5. Go to Credentials tab
6. Set Password: `password`
7. Temporary: OFF
8. Click Set Password

### 6. Create Roles (Optional)

1. Navigate to Realm roles → Create role
2. Role name: `USER`
3. Click Save
4. Create another role: `ADMIN`
5. Assign roles to the test user:
   - Users → testuser → Role mapping → Assign role
   - Select USER and/or ADMIN

## Creating the Spring Boot Project

### 1. Initialize the Project

Use Spring Initializr (https://start.spring.io/) with:
- Project: Gradle - Kotlin
- Language: Kotlin
- Spring Boot: 3.2.0 or later
- Dependencies:
  - Spring Web
  - Spring Security
  - OAuth2 Resource Server

Or create a `build.gradle.kts`:

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
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
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### 2. Application Properties

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/myrealm
          jwk-set-uri: http://localhost:8080/realms/myrealm/protocol/openid-connect/certs

logging:
  level:
    org.springframework.security: DEBUG
```

## Configuring Spring Security with Keycloak

### 1. Main Application Class

```kotlin
package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ResourceServerApplication

fun main(args: Array<String>) {
    runApplication<ResourceServerApplication>(*args)
}
```

### 2. Security Configuration

Create `SecurityConfig.kt`:

```kotlin
package com.example.demo.config

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
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter())
        return converter
    }

    @Bean
    fun jwtGrantedAuthoritiesConverter(): Converter<Jwt, Collection<GrantedAuthority>> {
        return KeycloakRealmRoleConverter()
    }
}

class KeycloakRealmRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.claims["realm_access"] as? Map<String, Any> ?: return emptyList()
        val roles = realmAccess["roles"] as? List<String> ?: return emptyList()
        
        return roles.map { role ->
            SimpleGrantedAuthority("ROLE_$role")
        }
    }
}
```

## Creating Protected Resources

### 1. Create DTOs

```kotlin
package com.example.demo.dto

data class UserInfo(
    val username: String,
    val email: String?,
    val roles: List<String>
)

data class ApiResponse(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 2. Create Controllers

Create `PublicController.kt`:

```kotlin
package com.example.demo.controller

import com.example.demo.dto.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public")
class PublicController {

    @GetMapping("/health")
    fun health(): ApiResponse {
        return ApiResponse("Service is healthy")
    }

    @GetMapping("/info")
    fun info(): Map<String, String> {
        return mapOf(
            "service" to "Resource Server",
            "version" to "1.0.0",
            "description" to "Kotlin Spring Boot with Keycloak"
        )
    }
}
```

Create `UserController.kt`:

```kotlin
package com.example.demo.controller

import com.example.demo.dto.ApiResponse
import com.example.demo.dto.UserInfo
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping("/profile")
    fun getUserProfile(@AuthenticationPrincipal jwt: Jwt): UserInfo {
        return UserInfo(
            username = jwt.getClaimAsString("preferred_username") ?: jwt.subject,
            email = jwt.getClaimAsString("email"),
            roles = extractRoles(jwt)
        )
    }

    @GetMapping("/data")
    fun getUserData(): ApiResponse {
        return ApiResponse("This is user data - accessible to USER and ADMIN roles")
    }

    @PostMapping("/action")
    @PreAuthorize("hasRole('USER')")
    fun performUserAction(@RequestBody action: Map<String, String>): ApiResponse {
        return ApiResponse("User action performed: ${action["name"]}")
    }

    private fun extractRoles(jwt: Jwt): List<String> {
        val realmAccess = jwt.claims["realm_access"] as? Map<String, Any> ?: return emptyList()
        return realmAccess["roles"] as? List<String> ?: emptyList()
    }
}
```

Create `AdminController.kt`:

```kotlin
package com.example.demo.controller

import com.example.demo.dto.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController {

    @GetMapping("/users")
    fun getAllUsers(): ApiResponse {
        return ApiResponse("Admin endpoint - List of all users")
    }

    @PostMapping("/config")
    fun updateConfiguration(@RequestBody config: Map<String, Any>): ApiResponse {
        return ApiResponse("Configuration updated by admin")
    }

    @DeleteMapping("/cache")
    fun clearCache(): ApiResponse {
        return ApiResponse("Cache cleared by admin")
    }
}
```

### 3. Create a Service Layer (Optional)

```kotlin
package com.example.demo.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthService {

    fun getCurrentUser(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val jwt = authentication.principal as Jwt
        return jwt.getClaimAsString("preferred_username") ?: jwt.subject
    }

    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.authorities.any { it.authority == "ROLE_$role" }
    }

    fun getUserEmail(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        val jwt = authentication.principal as Jwt
        return jwt.getClaimAsString("email")
    }
}
```

## Testing the Application

### 1. Obtain an Access Token

First, get a token from Keycloak using the password grant type:

```bash
curl -X POST http://localhost:8080/realms/myrealm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=spring-boot-app" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "username=testuser" \
  -d "password=password"
```

### 2. Test Public Endpoints

```bash
# No authentication required
curl http://localhost:8081/api/public/health

curl http://localhost:8081/api/public/info
```

### 3. Test Protected Endpoints

```bash
# Replace YOUR_ACCESS_TOKEN with the token from step 1
export TOKEN="YOUR_ACCESS_TOKEN"

# Test user endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/user/profile

# Test user data endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/user/data

# Test admin endpoint (will fail if user doesn't have ADMIN role)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/admin/users
```

### 4. Create Integration Tests

```kotlin
package com.example.demo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ResourceServerApplicationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `public endpoint should be accessible without authentication`() {
        mockMvc.perform(get("/api/public/health"))
            .andExpect(status().isOk)
    }

    @Test
    fun `protected endpoint should return 401 without authentication`() {
        mockMvc.perform(get("/api/user/profile"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `user endpoint should be accessible with USER role`() {
        mockMvc.perform(get("/api/user/data"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `admin endpoint should return 403 with only USER role`() {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin endpoint should be accessible with ADMIN role`() {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk)
    }
}
```

## Advanced Configuration

### 1. Custom Token Enhancer

Add custom claims to your tokens in Keycloak:

```kotlin
package com.example.demo.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class CustomJwtConverter : Converter<Jwt, CustomPrincipal> {
    override fun convert(jwt: Jwt): CustomPrincipal {
        return CustomPrincipal(
            username = jwt.getClaimAsString("preferred_username") ?: jwt.subject,
            email = jwt.getClaimAsString("email"),
            fullName = jwt.getClaimAsString("name"),
            roles = extractRoles(jwt),
            jwt = jwt
        )
    }

    private fun extractRoles(jwt: Jwt): Set<String> {
        val realmAccess = jwt.claims["realm_access"] as? Map<String, Any> ?: return emptySet()
        val roles = realmAccess["roles"] as? List<String> ?: return emptySet()
        return roles.toSet()
    }
}

data class CustomPrincipal(
    val username: String,
    val email: String?,
    val fullName: String?,
    val roles: Set<String>,
    val jwt: Jwt
)
```

### 2. CORS Configuration

```kotlin
package com.example.demo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "http://localhost:4200")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

### 3. Exception Handling

```kotlin
package com.example.demo.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                error = "Authentication Failed",
                message = ex.message ?: "Invalid authentication credentials",
                status = HttpStatus.UNAUTHORIZED.value()
            ),
            HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                error = "Access Denied",
                message = "You don't have permission to access this resource",
                status = HttpStatus.FORBIDDEN.value()
            ),
            HttpStatus.FORBIDDEN
        )
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
    val status: Int,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 4. Logging Configuration

Add to `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.example.demo: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### 5. Production Considerations

For production deployment:

1. **Use HTTPS**: Always use HTTPS in production
2. **Secure Keycloak**: Don't use development mode
3. **Token Validation**: Configure proper token validation
4. **Rate Limiting**: Implement rate limiting
5. **Monitoring**: Add metrics and monitoring

Example production configuration:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.yourdomain.com/realms/production
          jwk-set-uri: https://auth.yourdomain.com/realms/production/protocol/openid-connect/certs
          
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**: Check token expiration and issuer-uri configuration
2. **403 Forbidden**: Verify role mappings in Keycloak and Spring Security
3. **Connection refused**: Ensure Keycloak is running and accessible
4. **Invalid token**: Check JWT signature and public key configuration

### Debug Tips

1. Enable debug logging for Spring Security
2. Use JWT debugger (jwt.io) to inspect tokens
3. Check Keycloak logs for authentication issues
4. Verify network connectivity between services

## Conclusion

You now have a fully functional Kotlin Spring Boot application acting as a resource server protected by Keycloak. This setup provides:

- OAuth 2.0/OpenID Connect authentication
- Role-based access control
- Stateless authentication using JWT tokens
- Integration with Keycloak as the identity provider

Next steps could include:
- Implementing refresh token handling
- Adding multi-tenancy support
- Integrating with a frontend application
- Implementing fine-grained permissions
- Adding audit logging