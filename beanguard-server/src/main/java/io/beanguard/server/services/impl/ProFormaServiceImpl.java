package io.beanguard.server.services.impl;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import io.beanguard.api.models.shop.OrderPeriod;
import io.beanguard.server.entities.OrderEntity;
import io.beanguard.server.entities.OrderItemEntity;
import io.beanguard.server.entities.ProFormaEntity;
import io.beanguard.server.models.ParameterName;
import io.beanguard.server.repositories.ProFormaRepository;
import io.beanguard.server.services.ParameterService;
import io.beanguard.server.services.ProFormaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProFormaServiceImpl implements ProFormaService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ParameterService parameterService;
    private final ProFormaNumberAllocator numberAllocator;
    private final ProFormaRepository proFormaRepository;

    @Override
    @Transactional
    public ProFormaEntity generate(OrderEntity order, LocalDate issueDate, UUID licenceKey, LocalDate projectedExpiry) {
        String number = numberAllocator.allocateNext(issueDate.getYear());

        int vatRate = Integer.parseInt(parameterService.getString(ParameterName.LEGAL_VAT_RATE));
        int paymentDays = Integer.parseInt(
                parameterService.getString(ParameterName.LEGAL_PRO_FORMA_PAYMENT_DAYS));

        String periodLabel = order.getPeriod() == OrderPeriod.ONE_YEAR ? "1 rok" : "1 miesiąc";
        BigDecimal netto = BigDecimal.ZERO;
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItemEntity item : order.getItems()) {
            BigDecimal lineNetto = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineVat = lineNetto.multiply(BigDecimal.valueOf(vatRate))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineBrutto = lineNetto.add(lineVat);
            netto = netto.add(lineNetto);
            itemsHtml.append("<tr>")
                    .append("<td>").append(escapeXml(item.getName())).append(" – ").append(periodLabel).append("</td>")
                    .append("<td style=\"text-align:center\">").append(item.getQuantity()).append(" szt.</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(item.getPrice())).append("</td>")
                    .append("<td style=\"text-align:center\">").append(vatRate).append("%</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(lineNetto)).append("</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(lineVat)).append("</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(lineBrutto)).append("</td>")
                    .append("</tr>");
        }
        BigDecimal credit = order.getCreditAmount();
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal creditNetto = credit.negate();
            BigDecimal creditVat = creditNetto.multiply(BigDecimal.valueOf(vatRate))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal creditBrutto = creditNetto.add(creditVat);
            netto = netto.add(creditNetto);
            itemsHtml.append("<tr>")
                    .append("<td>Kredyt za niewykorzystany czas licencji</td>")
                    .append("<td style=\"text-align:center\">1</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(creditNetto)).append("</td>")
                    .append("<td style=\"text-align:center\">").append(vatRate).append("%</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(creditNetto)).append("</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(creditVat)).append("</td>")
                    .append("<td style=\"text-align:right\">").append(fmt(creditBrutto)).append("</td>")
                    .append("</tr>");
        }
        BigDecimal vatTotal = netto.multiply(BigDecimal.valueOf(vatRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Map<String, String> vars = new HashMap<>();
        vars.put("number",      number);
        vars.put("issueDate",   issueDate.format(DATE_FMT));
        vars.put("paymentDue",  issueDate.plusDays(paymentDays).format(DATE_FMT));
        vars.put("sellerName",  escapeXml(parameterService.getString(ParameterName.LEGAL_COMPANY_NAME)));
        vars.put("sellerNip",   escapeXml(parameterService.getString(ParameterName.LEGAL_COMPANY_NIP)));
        vars.put("sellerAddress", escapeXml(parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS)));
        vars.put("sellerPhone", escapeXml(parameterService.getString(ParameterName.LEGAL_COMPANY_PHONE)));
        String krs = parameterService.getString(ParameterName.LEGAL_COMPANY_KRS).trim();
        vars.put("sellerKrsLine", krs.isEmpty() ? "" : "KRS: " + escapeXml(krs) + "<br/>");
        vars.put("bankAccount", escapeXml(parameterService.getString(ParameterName.LEGAL_BANK_ACCOUNT)));
        vars.put("buyerCompany",  escapeXml(nvl(order.getCompanyName())));
        vars.put("buyerNip",      escapeXml(order.getVatId()));
        vars.put("buyerStreet",   escapeXml(nvl(order.getStreet())));
        vars.put("buyerPostCode", escapeXml(nvl(order.getPostCode())));
        vars.put("buyerCity",     escapeXml(nvl(order.getCity())));
        vars.put("buyerEmail",    escapeXml(order.getEmail()));
        vars.put("itemsTable",    itemsHtml.toString());
        vars.put("nettoTotal",    fmt(netto));
        vars.put("vatRate",       String.valueOf(vatRate));
        vars.put("vatAmount",     fmt(vatTotal));
        vars.put("bruttoTotal",   fmt(netto.add(vatTotal)));
        vars.put("currency",      escapeXml(parameterService.getString(ParameterName.LEGAL_CURRENCY)));
        StringBuilder licenceSection = new StringBuilder("<div class=\"licence-info\">");
        if (licenceKey != null) {
            licenceSection.append("<strong>Numer licencji:</strong> ").append(licenceKey).append("<br/>");
        }
        licenceSection.append("<strong>Licencja aktywna do:</strong> ")
                      .append(projectedExpiry.format(DATE_FMT));
        licenceSection.append("</div>");
        vars.put("licenceSection", licenceSection.toString());

        String xhtml = TemplateRenderer.render(
                parameterService.getString(ParameterName.LEGAL_PRO_FORMA_TEMPLATE), vars);

        ProFormaEntity entity = new ProFormaEntity();
        entity.setOrder(order);
        entity.setNumber(number);
        entity.setPdf(renderPdf(xhtml));
        return proFormaRepository.save(entity);
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
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private static String fmt(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
