package io.beanguard.server

import io.beanguard.server.config.RateLimitInterceptor
import io.beanguard.server.models.ParameterName
import io.beanguard.server.services.ParameterService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

import java.io.PrintWriter
import java.io.StringWriter

class RateLimitInterceptorTest extends Specification {

    ParameterService parameterService = Mock()
    RateLimitInterceptor interceptor

    def setup() {
        parameterService.getString(ParameterName.RATE_LIMIT_OPEN_RPM) >> "3"
        interceptor = new RateLimitInterceptor(parameterService)
    }

    def "allows requests up to the limit"() {
        given:
        def request = Mock(HttpServletRequest) { getRemoteAddr() >> "1.2.3.4" }
        def response = Mock(HttpServletResponse)

        expect:
        interceptor.preHandle(request, response, null) == true
        interceptor.preHandle(request, response, null) == true
        interceptor.preHandle(request, response, null) == true
    }

    def "blocks the request after limit is exceeded and returns 429"() {
        given:
        def writer = new StringWriter()
        def request = Mock(HttpServletRequest) { getRemoteAddr() >> "2.3.4.5" }
        def response = Mock(HttpServletResponse) { getWriter() >> new PrintWriter(writer) }

        when:
        3.times { interceptor.preHandle(request, response, null) }
        def result = interceptor.preHandle(request, response, null)

        then:
        !result
        1 * response.setStatus(429)
        writer.toString().contains("Too many requests")
    }

    def "separate IPs have independent buckets"() {
        given:
        def writer = new StringWriter()
        def reqA = Mock(HttpServletRequest) { getRemoteAddr() >> "10.0.0.1" }
        def respA = Mock(HttpServletResponse) { getWriter() >> new PrintWriter(writer) }
        def reqB = Mock(HttpServletRequest) { getRemoteAddr() >> "10.0.0.2" }
        def respB = Mock(HttpServletResponse)

        when:
        3.times { interceptor.preHandle(reqA, respA, null) }
        def resultA = interceptor.preHandle(reqA, respA, null)
        def resultB = interceptor.preHandle(reqB, respB, null)

        then:
        !resultA
        resultB == true
    }

    def "uses first IP from X-Forwarded-For header"() {
        given:
        def writer = new StringWriter()
        def request = Mock(HttpServletRequest) {
            getHeader("X-Forwarded-For") >> "5.6.7.8, 192.168.1.1"
            getRemoteAddr() >> "192.168.1.1"
        }
        def response = Mock(HttpServletResponse) { getWriter() >> new PrintWriter(writer) }

        when:
        3.times { interceptor.preHandle(request, response, null) }
        def result = interceptor.preHandle(request, response, null)

        then:
        !result
        1 * response.setStatus(429)
    }
}
