package dev.beanguard.client.registries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LicenceStatus {
    NOT_LOADED(false, false),
    MISSING_KEY(false, false),
    WRONG_KEY(false, false),
    WRONG_SECRET(false, false),
    EXPIRED(false, true),
    LOADED(true, true);
    private final boolean valid;
    private final boolean loaded;
}
