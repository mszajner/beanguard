package dev.beanguard.server.mappers;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceCreateRequest;
import dev.beanguard.api.models.licence.LicenceUpdateRequest;
import dev.beanguard.api.models.shop.ShopLicenceContext;
import dev.beanguard.server.entities.LicenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LicenceMapper {

    Licence toDto(LicenceEntity licenceEntity);

    @Mapping(target = "secret", ignore = true)
    Licence withoutSecret(LicenceEntity licenceEntity);

    @Mapping(target = "licenceId", source = "key")
    @Mapping(target = "limitClaims", ignore = true)
    @Mapping(target = "featureClaims", ignore = true)
    @Mapping(target = "licenceNetAmount", source = "netAmount")
    @Mapping(target = "licenceLastPeriod", source = "lastPeriod")
    @Mapping(target = "licenceType", source = "type")
    ShopLicenceContext toShopContext(LicenceEntity licenceEntity);

    @Mapping(target = "key", ignore = true)
    @Mapping(target = "secret", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "netAmount", ignore = true)
    @Mapping(target = "lastPeriod", ignore = true)
    LicenceEntity toEntity(LicenceCreateRequest licence);

    @Mapping(target = "key", ignore = true)
    @Mapping(target = "secret", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "netAmount", ignore = true)
    @Mapping(target = "lastPeriod", ignore = true)
    LicenceEntity toEntity(LicenceUpdateRequest licence);
}
