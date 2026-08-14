package dev.beanguard.server.services.impl;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import dev.beanguard.api.models.shop.OrderPeriod;
import dev.beanguard.server.entities.LicenceEntity;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.ports.InstantProvider;
import dev.beanguard.server.repositories.ProductRepository;
import dev.beanguard.server.services.CertificateService;
import dev.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");

    private final ParameterService parameterService;
    private final InstantProvider instantProvider;
    private final ProductRepository productRepository;

    @Override
    public byte[] generate(LicenceEntity licence) {
        Map<String, String> vars = new HashMap<>();
        vars.put("licenceKey",   licence.getKey().toString());
        vars.put("companyName",  escapeXml(nvl(licence.getCompanyName(), licence.getEmail())));
        vars.put("vatId",        escapeXml(nvl(licence.getVatId(), "")));
        vars.put("expiration",   licence.getExpiration() != null
                ? licence.getExpiration().atZone(ZONE).toLocalDate().format(DATE_FMT)
                : "Bezterminowa");
        vars.put("issueDate",    instantProvider.now().atZone(ZONE).toLocalDate().format(DATE_FMT));
        vars.put("sellerName",   escapeXml(parameterService.getString(ParameterName.LEGAL_COMPANY_NAME)));
        vars.put("sellerNip",    escapeXml(parameterService.getString(ParameterName.LEGAL_COMPANY_NIP)));
        vars.put("productName",  escapeXml(parameterService.getString(ParameterName.BRANDING_TITLE)));
        vars.put("activationText", escapeXml(parameterService.getString(ParameterName.LEGAL_CERTIFICATE_ACTIVATION_TEXT)));

        String logoUrl = parameterService.getString(ParameterName.BRANDING_LOGO_URL).trim();
        vars.put("logoSection", logoUrl.isEmpty()
                ? "" : "<img src=\"" + escapeXml(logoUrl) + "\" style=\"height:24pt;\"/>");

        String websiteUrl = parameterService.getString(ParameterName.BRANDING_WEBSITE_URL).trim();
        vars.put("websiteUrl", escapeXml(websiteUrl));

        if (licence.getLastPeriod() != null) {
            String label = licence.getLastPeriod() == OrderPeriod.ONE_YEAR ? "Roczna" : "Miesi&#281;czna";
            vars.put("periodRow",
                    "<tr><td class=\"lbl\">Okres subskrypcji:</td><td>" + label + "</td></tr>");
        } else {
            vars.put("periodRow", "");
        }

        if (licence.getClaims() != null && !licence.getClaims().isEmpty()) {
            Map<String, String> claimLabels = productRepository.findAllByOrderBySortOrderAsc().stream()
                    .filter(p -> p.getCertificateName() != null && !p.getCertificateName().isBlank())
                    .collect(java.util.stream.Collectors.toMap(
                            dev.beanguard.server.entities.ProductEntity::getClaim,
                            dev.beanguard.server.entities.ProductEntity::getCertificateName,
                            (a, b) -> a));
            StringBuilder claims = new StringBuilder();
            licence.getClaims().forEach((k, v) -> {
                String label = claimLabels.getOrDefault(k, k);
                claims.append("<tr><td class=\"lbl\">").append(escapeXml(label)).append(":</td>")
                      .append("<td>").append(escapeXml(v)).append("</td></tr>");
            });
            vars.put("claimsSection", claims.toString());
        } else {
            vars.put("claimsSection", "");
        }

        String xhtml = TemplateRenderer.render(
                parameterService.getString(ParameterName.LEGAL_CERTIFICATE_TEMPLATE), vars);
        return renderPdf(xhtml);
    }

    private byte[] renderPdf(String xhtml) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            URL fontUrl = getClass().getResource("/fonts/DejaVuSans.ttf");
            if (fontUrl != null) {
                renderer.getFontResolver().addFont(
                        fontUrl.toExternalForm(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Certificate PDF generation failed", e);
        }
    }

    private static String nvl(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
