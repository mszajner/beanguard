package io.beanguard.server.controllers.open;

import io.beanguard.server.models.ParameterName;
import io.beanguard.server.services.ParameterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open/config")
@RequiredArgsConstructor
@Tag(name = "Config")
public class OpenConfigController {

    private final ParameterService parameterService;

    @GetMapping
    public ConfigResponse getConfig() {
        String logoUrl     = parameterService.getString(ParameterName.BRANDING_LOGO_URL);
        String faviconUrl  = parameterService.getString(ParameterName.BRANDING_FAVICON_URL);
        String supportEmail = parameterService.getString(ParameterName.BRANDING_SUPPORT_EMAIL);
        String termsUrl    = parameterService.getString(ParameterName.LEGAL_TERMS_URL);
        String privacyUrl  = parameterService.getString(ParameterName.LEGAL_PRIVACY_URL);
        String vatRateStr  = parameterService.getString(ParameterName.LEGAL_VAT_RATE);
        String companyName = parameterService.getString(ParameterName.LEGAL_COMPANY_NAME);
        String companyNip  = parameterService.getString(ParameterName.LEGAL_COMPANY_NIP);
        String companyKrs  = parameterService.getString(ParameterName.LEGAL_COMPANY_KRS);
        String companyAddress = parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS);
        String companyPhone   = parameterService.getString(ParameterName.LEGAL_COMPANY_PHONE);

        Integer vatRate = vatRateStr.isBlank() ? null : Integer.parseInt(vatRateStr);

        return new ConfigResponse(
            parameterService.getString(ParameterName.BRANDING_TITLE),
            logoUrl.isBlank()      ? null : logoUrl,
            faviconUrl.isBlank()   ? null : faviconUrl,
            supportEmail.isBlank() ? null : supportEmail,
            parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR),
            parameterService.getString(ParameterName.LEGAL_CURRENCY),
            termsUrl.isBlank()     ? null : termsUrl,
            privacyUrl.isBlank()   ? null : privacyUrl,
            vatRate,
            companyName.isBlank()    ? null : companyName,
            companyNip.isBlank()     ? null : companyNip,
            companyKrs.isBlank()     ? null : companyKrs,
            companyAddress.isBlank() ? null : companyAddress,
            companyPhone.isBlank()   ? null : companyPhone
        );
    }

    public record ConfigResponse(
        String title,
        String logoUrl,
        String faviconUrl,
        String supportEmail,
        String primaryColor,
        String currency,
        String termsUrl,
        String privacyUrl,
        Integer vatRate,
        String companyName,
        String companyNip,
        String companyKrs,
        String companyAddress,
        String companyPhone
    ) {}
}
