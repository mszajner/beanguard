package dev.beanguard.server

import dev.beanguard.server.models.ParameterName

/**
 * Regression tests for the unauthenticated licence takeover reported against 0.1.4:
 * the initiate response leaked the token that confirms the transfer, so the email
 * round-trip proving ownership of the licence could be skipped.
 */
class LicenceTransferSecurityAcceptanceSpec extends TransferFlowSpec {

    def setup() {
        parameterService.setString(ParameterName.TRANSFER_INIT_COOLDOWN_SECONDS, "60")
    }

    def "token returned to the initiator cannot confirm the transfer"() {
        given:
        def licence = newLicence("sec-takeover@example.com")

        when: "attacker knowing only the licence key initiates and tries to confirm with the response token"
        def init = initiate(licence.key)
        def confirmed = confirm(init.body.get("transferToken").asText())

        then: "confirmation is refused and nothing was rotated"
        confirmed.code == 404
        poll(init.body.get("transferToken").asText()).body.get("status").asText() == "PENDING"
        licenceDetailsCode(licence.key, licence.secret) == 200
    }

    def "confirmation token from the email cannot be used to poll for the secret"() {
        given:
        def licence = newLicence("sec-confirm-poll@example.com")
        initiate(licence.key)
        def mailed = mailedConfirmToken("sec-confirm-poll@example.com")

        expect:
        mailed != null
        poll(mailed).code == 404
    }

    def "owner confirming by email lets only the initiator collect the new secret, once"() {
        given:
        def licence = newLicence("sec-owner@example.com")
        def init = initiate(licence.key)
        def pollToken = init.body.get("transferToken").asText()
        def mailed = mailedConfirmToken("sec-owner@example.com")

        when: "owner confirms from the email"
        def confirmed = confirm(mailed)

        then: "the confirmation response carries no secret and the old secret still works"
        confirmed.code == 200
        confirmed.body == null || confirmed.body.get("secret") == null
        licenceDetailsCode(licence.key, licence.secret) == 200

        when: "the initiator collects the secret"
        def collected = poll(pollToken)
        def newSecret = collected.body.get("secret").asText()

        then:
        collected.body.get("status").asText() == "CONFIRMED"
        newSecret != licence.secret
        licenceDetailsCode(licence.key, newSecret) == 200
        licenceDetailsCode(licence.key, licence.secret) == 404

        and: "the secret can be collected only once"
        poll(pollToken).code == 404
    }

    def "a confirmation token cannot be confirmed twice"() {
        given:
        def licence = newLicence("sec-twice@example.com")
        initiate(licence.key)
        def mailed = mailedConfirmToken("sec-twice@example.com")
        confirm(mailed)

        expect:
        confirm(mailed).code == 410
    }

    def "repeated initiation within the cooldown sends no further email and keeps the first transfer"() {
        given:
        def licence = newLicence("sec-cooldown@example.com")
        def mailsBefore = mailCount("sec-cooldown@example.com")
        def first = initiate(licence.key)

        when:
        def second = initiate(licence.key)

        then:
        second.code == 200
        mailCount("sec-cooldown@example.com") == mailsBefore + 1
        poll(first.body.get("transferToken").asText()).body.get("status").asText() == "PENDING"
        poll(second.body.get("transferToken").asText()).code == 404
    }

    def "after the cooldown a new initiation replaces the pending transfer"() {
        given:
        parameterService.setString(ParameterName.TRANSFER_INIT_COOLDOWN_SECONDS, "0")
        def licence = newLicence("sec-replace@example.com")
        def first = initiate(licence.key)

        when:
        def second = initiate(licence.key)

        then:
        poll(first.body.get("transferToken").asText()).code == 404
        poll(second.body.get("transferToken").asText()).body.get("status").asText() == "PENDING"
    }

    def "initiation does not reveal whether a licence key exists or has an email"() {
        given:
        def noEmail = newLicence("")

        when:
        def unknown = initiate("00000000-0000-0000-0000-000000000000")
        def withoutEmail = initiate(noEmail.key)

        then: "same successful response shape, and the returned token is useless"
        unknown.code == 200
        withoutEmail.code == 200
        unknown.body.get("transferToken") != null
        unknown.body.get("expiresAt") != null
        withoutEmail.body.get("transferToken") != null
        poll(unknown.body.get("transferToken").asText()).code == 404
        poll(withoutEmail.body.get("transferToken").asText()).code == 404
    }
}
