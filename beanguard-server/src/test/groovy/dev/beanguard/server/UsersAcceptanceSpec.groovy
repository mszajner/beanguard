package dev.beanguard.server

import dev.beanguard.server.controllers.UsersController
import dev.beanguard.server.exceptions.LastAdminDeletionException
import dev.beanguard.server.models.Role
import dev.beanguard.server.models.UserCreateRequest
import dev.beanguard.server.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired

import java.util.Set

class UsersAcceptanceSpec extends IntegrationSpec {

    @Autowired UsersController usersController
    @Autowired UserRepository userRepository

    def "deleteUser throws LastAdminDeletionException when deleting the last admin"() {
        given:
        def admins = userRepository.findAll().findAll { it.roles.contains(Role.ADMIN) }
        def lastAdminId = admins[0].id

        when:
        usersController.deleteUser(lastAdminId)

        then:
        thrown(LastAdminDeletionException)
    }

    def "deleteUser succeeds when another admin exists"() {
        given:
        def secondAdmin = usersController.createUser(
                new UserCreateRequest("Second", "Admin", "second.admin@example.com", "Pass123!", Set.of(Role.ADMIN)))

        when:
        usersController.deleteUser(secondAdmin.id)

        then:
        noExceptionThrown()
    }
}
