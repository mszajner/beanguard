package io.beanguard.server

import io.beanguard.server.entities.ProFormaSequenceEntity
import io.beanguard.server.repositories.ProFormaSequenceRepository
import io.beanguard.server.services.impl.ProFormaNumberAllocator
import spock.lang.Specification

class ProFormaNumberAllocatorTest extends Specification {

    ProFormaSequenceRepository repository = Mock()
    ProFormaNumberAllocator allocator = new ProFormaNumberAllocator(repository)

    def "allocateNext returns PF/year/001 when no row exists for that year"() {
        given:
        repository.findByYearForUpdate(2026) >> Optional.empty()
        repository.save(_ as ProFormaSequenceEntity) >> { ProFormaSequenceEntity e -> e }

        when:
        def number = allocator.allocateNext(2026)

        then:
        number == "PF/2026/001"
    }

    def "allocateNext increments from existing counter"() {
        given:
        repository.findByYearForUpdate(2026) >> Optional.of(new ProFormaSequenceEntity(2026, 5))
        repository.save(_ as ProFormaSequenceEntity) >> { ProFormaSequenceEntity e -> e }

        when:
        def number = allocator.allocateNext(2026)

        then:
        number == "PF/2026/006"
    }

    def "allocateNext formats number with leading zeros"() {
        given:
        repository.findByYearForUpdate(2026) >> Optional.of(new ProFormaSequenceEntity(2026, 9))
        repository.save(_ as ProFormaSequenceEntity) >> { ProFormaSequenceEntity e -> e }

        when:
        def number = allocator.allocateNext(2026)

        then:
        number == "PF/2026/010"
    }

    def "allocateNext maintains independent sequences per year"() {
        given:
        repository.findByYearForUpdate(2025) >> Optional.of(new ProFormaSequenceEntity(2025, 10))
        repository.findByYearForUpdate(2026) >> Optional.empty()
        repository.save(_ as ProFormaSequenceEntity) >> { ProFormaSequenceEntity e -> e }

        expect:
        allocator.allocateNext(2025) == "PF/2025/011"
        allocator.allocateNext(2026) == "PF/2026/001"
    }
}
