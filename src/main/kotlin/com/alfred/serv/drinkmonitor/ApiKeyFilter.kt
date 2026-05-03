package com.alfred.serv.drinkmonitor

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ApiKeyFilter : Filter {

    @Value("\${app.api.key}")
    lateinit var apiKey: String

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse

        // Check if the request is an API request (e.g., starts with /api/)
        if (req.requestURI.startsWith("/api/")) {
            val keyFromHeader = req.getHeader("X-API-KEY")
            val keyFromParam = req.getParameter("key")

            val providedKey = keyFromHeader ?: keyFromParam

            if (providedKey == null || providedKey != apiKey) {
                res.status = HttpServletResponse.SC_UNAUTHORIZED
                res.writer.write("Unauthorized: Invalid API Key")
                return
            }
        }

        // Continue processing the request
        chain.doFilter(request, response)
    }
}
