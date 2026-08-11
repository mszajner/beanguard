package io.beanguard.api.models.shop;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
@Data
public class LicenceTokenResponse {
    String shopUrl;
    Instant expiresAt;
}
