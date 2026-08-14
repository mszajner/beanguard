package dev.beanguard.server.services.impl

import dev.beanguard.server.models.ParameterName
import dev.beanguard.server.models.Role
import dev.beanguard.server.repositories.UserRepository
import dev.beanguard.server.services.ParameterService
import org.springframework.core.env.Environment
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class ServerInitializationServiceTest extends Specification {

    def parameterService = Mock(ParameterService)
    def userRepository = Mock(UserRepository)
    def passwordEncoder = Mock(PasswordEncoder)
    def environment = Mock(Environment)
    def service = new ServerInitializationService(parameterService, userRepository, passwordEncoder, environment)

    def "run() throws IllegalStateException when prod profile active and show_sql=true"() {
        given:
        parameterService.getString(_) >> "existing-key"
        userRepository.countByRolesContaining(Role.ADMIN) >> 1
        environment.getActiveProfiles() >> (["prod"] as String[])
        environment.getProperty("spring.jpa.show_sql") >> "true"
        environment.getProperty("BEANGUARD_SUPPORT_EMAIL", "") >> "support@example.com"

        when:
        service.run(null)

        then:
        def ex = thrown(IllegalStateException)
        ex.message.contains("show_sql=true")
    }

    def "run() does not throw when prod profile active and show_sql=false"() {
        given:
        parameterService.getString(_) >> "existing-key"
        userRepository.countByRolesContaining(Role.ADMIN) >> 1
        environment.getActiveProfiles() >> (["prod"] as String[])
        environment.getProperty("spring.jpa.show_sql") >> "false"
        environment.getProperty("BEANGUARD_SUPPORT_EMAIL", "") >> "support@example.com"

        when:
        service.run(null)

        then:
        notThrown(Exception)
    }

    def "run() does not throw when MAIL_HOST is empty"() {
        given:
        parameterService.getString(ParameterName.MAIL_HOST) >> ""
        parameterService.getString(_) >> "existing-key"
        userRepository.countByRolesContaining(Role.ADMIN) >> 1
        environment.getActiveProfiles() >> ([] as String[])
        environment.getProperty("spring.jpa.show_sql") >> "false"
        environment.getProperty("BEANGUARD_SUPPORT_EMAIL", "") >> "support@example.com"

        when:
        service.run(null)

        then:
        notThrown(Exception)
    }

    def "run() creates an admin user with an encoded random password when none exists"() {
        given:
        parameterService.getString(_) >> "existing-key"
        userRepository.countByRolesContaining(Role.ADMIN) >> 0
        passwordEncoder.encode(_) >> "encoded-password"
        environment.getActiveProfiles() >> ([] as String[])
        environment.getProperty("spring.jpa.show_sql") >> "false"

        when:
        service.run(null)

        then:
        1 * userRepository.save({ it.email == "admin@beanguard.dev" && it.password == "encoded-password" && it.roles == [Role.ADMIN] as Set })
    }

    def "run() does not create an admin user when one already exists"() {
        given:
        parameterService.getString(_) >> "existing-key"
        userRepository.countByRolesContaining(Role.ADMIN) >> 1
        environment.getActiveProfiles() >> ([] as String[])
        environment.getProperty("spring.jpa.show_sql") >> "false"

        when:
        service.run(null)

        then:
        0 * userRepository.save(_)
    }
}
