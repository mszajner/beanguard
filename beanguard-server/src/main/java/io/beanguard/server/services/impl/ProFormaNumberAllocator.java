package io.beanguard.server.services.impl;

import io.beanguard.server.entities.ProFormaSequenceEntity;
import io.beanguard.server.repositories.ProFormaSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProFormaNumberAllocator {

    private final ProFormaSequenceRepository repository;

    @Transactional
    public String allocateNext(int year) {
        repository.initializeYear(year);
        ProFormaSequenceEntity seq = repository.findByYearForUpdate(year)
                .orElseGet(() -> new ProFormaSequenceEntity(year, 0));
        seq.setLastNumber(seq.getLastNumber() + 1);
        repository.save(seq);
        return "PF/%d/%03d".formatted(year, seq.getLastNumber());
    }
}
