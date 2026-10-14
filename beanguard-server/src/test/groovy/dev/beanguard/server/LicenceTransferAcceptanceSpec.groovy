package dev.beanguard.server

import dev.beanguard.server.models.ParameterName

class LicenceTransferAcceptanceSpec extends TransferFlowSpec {

    def setup() {
        parameterService.setString(ParameterName.TRANSFER_INIT_COOLDOWN_SECONDS, "60")
    }

    def "happy path: initiate -> poll PENDING -> owner confirms by email -> poll CONFIRMED with new secret"() {
        given:
        def licence = newLicence("transfer@example.com")

        when: "initiate transfer"
        def init = initiate(licence.key)

        then:
        init.code == 200
        def pollToken = init.body.get("transferToken").asText()
        !pollToken.isBlank()
        init.body.get("expiresAt") != null

        when: "poll before the owner confirms"
        def pending = poll(pollToken)

        then:
        pending.code == 200
        pending.body.get("status").asText() == "PENDING"
        pending.body.get("secret") == null

        when: "owner confirms from the emailed link"
        def confirmed = confirm(mailedConfirmToken("transfer@example.com"))

        then:
        confirmed.code == 200

        when: "initiator polls again"
        def collected = poll(pollToken)

        then:
        collected.code == 200
        collected.body.get("status").asText() == "CONFIRMED"
        def newSecret = collected.body.get("secret").asText()
        !newSecret.isBlank()
        newSecret != licence.secret
    }

    def "old credentials fail after transfer, new credentials succeed"() {
        given:
        def licence = newLicence("transfer2@example.com")
        def pollToken = initiate(licence.key).body.get("transferToken").asText()
        confirm(mailedConfirmToken("transfer2@example.com"))
        def newSecret = poll(pollToken).body.get("secret").asText()

        expect:
        licenceDetailsCode(licence.key, licence.secret) == 404
        licenceDetailsCode(licence.key, newSecret) == 200
    }

    def "confirm already-confirmed token returns 410"() {
        given:
        def licence = newLicence("transfer4@example.com")
        initiate(licence.key)
        def confirmToken = mailedConfirmToken("transfer4@example.com")
        confirm(confirmToken)

        expect:
        confirm(confirmToken).code == 410
    }

    def "confirm unknown token returns 404"() {
        expect:
        confirm("00000000-0000-0000-0000-000000000000").code == 404
    }

    def "poll unknown token returns 404"() {
        expect:
        poll("00000000-0000-0000-0000-000000000000").code == 404
    }
}
