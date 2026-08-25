package org.example.bankramenserver.global.error

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class GlobalExceptionFilter(private val objectMapper: ObjectMapper) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try { filterChain.doFilter(request, response) }
        catch (e: GlobalException) { log.error("GlobalException in Filter: {}", e.message); writeErrorResponse(response, e.errorCode) }
        catch (e: Exception) { log.error("UnhandledException in Filter: {}", e.message); writeErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR) }
    }
    private fun writeErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.status.value(); response.contentType = MediaType.APPLICATION_JSON_VALUE; response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)))
    }
}
