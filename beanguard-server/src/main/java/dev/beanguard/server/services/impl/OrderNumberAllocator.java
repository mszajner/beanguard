package dev.beanguard.server.services.impl;

import dev.beanguard.server.entities.OrderSequenceEntity;
import dev.beanguard.server.repositories.OrderSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderNumberAllocator {

    private final OrderSequenceRepository repository;

    @Transactional
    public String allocateNext(int year) {
        repository.initializeYear(year);
        OrderSequenceEntity seq = repository.findByYearForUpdate(year)
                .orElseGet(() -> new OrderSequenceEntity(year, 0));
        seq.setLastNumber(seq.getLastNumber() + 1);
        repository.save(seq);
        return "ZAM/%d/%03d".formatted(year, seq.getLastNumber());
    }
}
