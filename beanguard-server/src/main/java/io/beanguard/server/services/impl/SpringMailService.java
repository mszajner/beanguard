package io.beanguard.server.services.impl;

import io.beanguard.api.models.licence.LicenceType;
import io.beanguard.api.models.shop.Order;
import io.beanguard.api.models.shop.OrderItem;
import io.beanguard.api.models.shop.OrderPeriod;
import io.beanguard.server.models.ParameterName;
import io.beanguard.server.services.MailService;
import io.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SpringMailService implements MailService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Warsaw"));

    private final ParameterService parameterService;

    @Override
    public void sendDemoWelcome(String to, String licenceToken, Instant expiration) {
        String host = parameterService.getString(ParameterName.MAIL_HOST);
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST not configured — skipping email to {}", to);
            return;
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("licenceToken",   licenceToken);
        vars.put("email",          to);
        vars.put("expirationDate", DATE_FMT.format(expiration));
        vars.put("productName",    parameterService.getString(ParameterName.BRANDING_TITLE));
        vars.put("logoUrl",        parameterService.getString(ParameterName.BRANDING_LOGO_URL));
        vars.put("accentColor",    parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR));
        vars.put("companyName",    parameterService.getString(ParameterName.LEGAL_COMPANY_NAME));
        vars.put("companyAddress", parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS));
        String subject = TemplateRenderer.render(
                parameterService.getString(ParameterName.MAIL_DEMO_WELCOME_SUBJECT), vars);
        String body = TemplateRenderer.render(
                parameterService.getString(ParameterName.MAIL_DEMO_WELCOME_BODY), vars);
        send(host, to, subject, body);
    }

    @Override
    public void sendExpiryWarning(String to, String companyName, Instant expiration, int daysLeft, LicenceType type) {
        String host = parameterService.getString(ParameterName.MAIL_HOST);
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST not configured — skipping email to {}", to);
            return;
        }
        ParameterName subjectParam = (type == LicenceType.DEMO)
                ? ParameterName.MAIL_EXPIRY_WARNING_SUBJECT_DEMO
                : ParameterName.MAIL_EXPIRY_WARNING_SUBJECT;
        ParameterName bodyParam = (type == LicenceType.DEMO)
                ? ParameterName.MAIL_EXPIRY_WARNING_BODY_DEMO
                : ParameterName.MAIL_EXPIRY_WARNING_BODY;
        Map<String, String> vars = new HashMap<>();
        vars.put("email",          to);
        vars.put("buyerCompany",   companyName != null ? companyName : "");
        vars.put("expirationDate", DATE_FMT.format(expiration));
        vars.put("daysLeft",       String.valueOf(daysLeft));
        vars.put("productName",    parameterService.getString(ParameterName.BRANDING_TITLE));
        vars.put("logoUrl",        parameterService.getString(ParameterName.BRANDING_LOGO_URL));
        vars.put("accentColor",    parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR));
        vars.put("companyName",    parameterService.getString(ParameterName.LEGAL_COMPANY_NAME));
        vars.put("companyAddress", parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS));
        String subject = TemplateRenderer.render(parameterService.getString(subjectParam), vars);
        String body    = TemplateRenderer.render(parameterService.getString(bodyParam), vars);
        send(host, to, subject, body);
    }

    @Override
    public void sendTransferConfirmation(String to, String confirmUrl) {
        String host = parameterService.getString(ParameterName.MAIL_HOST);
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST not configured — skipping transfer confirmation email to {}", to);
            return;
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("confirmUrl",     confirmUrl);
        vars.put("productName",    parameterService.getString(ParameterName.BRANDING_TITLE));
        vars.put("logoUrl",        parameterService.getString(ParameterName.BRANDING_LOGO_URL));
        vars.put("accentColor",    parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR));
        vars.put("companyName",    parameterService.getString(ParameterName.LEGAL_COMPANY_NAME));
        vars.put("companyAddress", parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS));
        String subject = TemplateRenderer.render(parameterService.getString(ParameterName.MAIL_TRANSFER_CONFIRM_SUBJECT), vars);
        String body    = TemplateRenderer.render(parameterService.getString(ParameterName.MAIL_TRANSFER_CONFIRM_BODY), vars);
        send(host, to, subject, body);
    }

    @Override
    public void sendLicenceCreated(String to, UUID licenceKey) {
        sendLicenceCreated(to, licenceKey, null);
    }

    @Override
    public void sendLicenceCreated(String to, UUID licenceKey, byte[] certificatePdf) {
        String host = parameterService.getString(ParameterName.MAIL_HOST);
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST not configured — skipping licence-created email to {}", to);
            return;
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("licenceKey",     licenceKey.toString());
        vars.put("productName",    parameterService.getString(ParameterName.BRANDING_TITLE));
        vars.put("logoUrl",        parameterService.getString(ParameterName.BRANDING_LOGO_URL));
        vars.put("accentColor",    parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR));
        vars.put("companyName",    parameterService.getString(ParameterName.LEGAL_COMPANY_NAME));
        vars.put("companyAddress", parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS));
        String subject = TemplateRenderer.render(parameterService.getString(ParameterName.MAIL_LICENCE_CREATED_SUBJECT), vars);
        String body    = TemplateRenderer.render(parameterService.getString(ParameterName.MAIL_LICENCE_CREATED_BODY), vars);
        if (certificatePdf != null) {
            sendWithAttachment(host, to, subject, body,
                    certificatePdf, "certyfikat-" + licenceKey + ".pdf");
        } else {
            send(host, to, subject, body);
        }
    }

    @Override
    public void sendOrderConfirmation(String to, Order order, byte[] proFormaPdf, String proFormaNumber) {
        String host = parameterService.getString(ParameterName.MAIL_HOST);
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST not configured — skipping order confirmation to {}", to);
            return;
        }

        String vatRateStr = parameterService.getString(ParameterName.LEGAL_VAT_RATE);
        int vatRate = vatRateStr.isBlank() ? 0 : Integer.parseInt(vatRateStr);

        BigDecimal netto = order.items().stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItem item : order.items()) {
            BigDecimal lineTotal = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            itemsHtml.append("<tr>")
                    .append("<td style=\"padding:6px 8px;border-bottom:1px solid #f4f4f5\">")
                    .append(escapeHtml(item.name()))
                    .append(item.quantity() > 1 ? " &times;" + item.quantity() : "")
                    .append("</td>")
                    .append("<td style=\"padding:6px 8px;border-bottom:1px solid #f4f4f5;text-align:right\">")
                    .append(lineTotal.toPlainString()).append("</td>")
                    .append("</tr>");
        }

        BigDecimal credit = order.creditAmount();
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal creditNetto = credit.negate();
            netto = netto.add(creditNetto);
            itemsHtml.append("<tr>")
                    .append("<td style=\"padding:6px 8px;border-bottom:1px solid #f4f4f5;color:#6b7280\">")
                    .append("Kredyt za niewykorzystany czas")
                    .append("</td>")
                    .append("<td style=\"padding:6px 8px;border-bottom:1px solid #f4f4f5;text-align:right;color:#6b7280\">")
                    .append(creditNetto.setScale(2, RoundingMode.HALF_UP).toPlainString()).append("</td>")
                    .append("</tr>");
        }

        BigDecimal vatAmount = netto.multiply(BigDecimal.valueOf(vatRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal brutto = netto.add(vatAmount);

        String periodLabel = switch (order.period()) {
            case ONE_YEAR -> "1 rok";
            case ONE_MONTH -> "1 miesiąc";
        };

        String sellerName    = parameterService.getString(ParameterName.LEGAL_COMPANY_NAME);
        String sellerNip     = parameterService.getString(ParameterName.LEGAL_COMPANY_NIP);
        String sellerAddress = parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS);
        String sellerContact = parameterService.getString(ParameterName.BRANDING_SUPPORT_EMAIL);
        String currency      = parameterService.getString(ParameterName.LEGAL_CURRENCY);

        Map<String, String> vars = new HashMap<>();
        vars.put("orderId",      order.number() != null ? order.number() : order.id().toString().substring(0, 8).toUpperCase());
        vars.put("orderDate",    DATE_FMT.format(order.createdAt()));
        vars.put("buyerEmail",   nvl(order.email()));
        vars.put("buyerVatId",   nvl(order.vatId()));
        vars.put("buyerCompany", nvl(order.companyName()));
        vars.put("period",       periodLabel);
        vars.put("itemsTable",   itemsHtml.toString());
        vars.put("nettoTotal",   netto.setScale(2, RoundingMode.HALF_UP).toPlainString());
        vars.put("vatRate",      String.valueOf(vatRate));
        vars.put("vatAmount",    vatAmount.toPlainString());
        vars.put("bruttoTotal",  brutto.toPlainString());
        vars.put("currency",     currency);
        vars.put("companyName",   sellerName);
        vars.put("sellerName",    sellerName);
        vars.put("sellerNip",     sellerNip.isBlank() ? "" : "NIP: " + sellerNip);
        vars.put("sellerAddress", sellerAddress);
        vars.put("sellerContact", sellerContact);
        vars.put("productName",   parameterService.getString(ParameterName.BRANDING_TITLE));
        vars.put("logoUrl",       parameterService.getString(ParameterName.BRANDING_LOGO_URL));
        vars.put("accentColor",   parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR));

        String subject = TemplateRenderer.render(
                parameterService.getString(ParameterName.MAIL_ORDER_CONFIRM_SUBJECT), vars);
        String body = TemplateRenderer.render(
                parameterService.getString(ParameterName.MAIL_ORDER_CONFIRM_BODY), vars);
        String attachmentName = "faktura-proforma-" + proFormaNumber.replace("/", "-") + ".pdf";
        sendWithAttachment(host, to, subject, body, proFormaPdf, attachmentName);
    }

    @Override
    public void sendOrderCancellation(String to, Order order) {
        String host = parameterService.getString(ParameterName.MAIL_HOST);
        if (host == null || host.isBlank()) {
            log.warn("MAIL_HOST not configured — skipping order cancellation email to {}", to);
            return;
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("orderId",      order.number() != null ? order.number() : order.id().toString().substring(0, 8).toUpperCase());
        vars.put("orderDate",    DATE_FMT.format(order.createdAt()));
        vars.put("productName",  parameterService.getString(ParameterName.BRANDING_TITLE));
        vars.put("logoUrl",      parameterService.getString(ParameterName.BRANDING_LOGO_URL));
        vars.put("accentColor",    parameterService.getString(ParameterName.BRANDING_PRIMARY_COLOR));
        vars.put("companyName",    parameterService.getString(ParameterName.LEGAL_COMPANY_NAME));
        vars.put("companyAddress", parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS));
        String subject = TemplateRenderer.render(
                parameterService.getString(ParameterName.MAIL_ORDER_CANCEL_SUBJECT), vars);
        String body = TemplateRenderer.render(
                parameterService.getString(ParameterName.MAIL_ORDER_CANCEL_BODY), vars);
        send(host, to, subject, body);
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void send(String host, String to, String subject, String htmlBody) {
        try {
            JavaMailSenderImpl sender = buildSender(host);
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(
                parameterService.getString(ParameterName.MAIL_FROM),
                parameterService.getString(ParameterName.MAIL_SENDER_NAME));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            sender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    private void sendWithAttachment(String host, String to, String subject, String htmlBody,
                                    byte[] pdfBytes, String attachmentFilename) {
        try {
            JavaMailSenderImpl sender = buildSender(host);
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(
                    parameterService.getString(ParameterName.MAIL_FROM),
                    parameterService.getString(ParameterName.MAIL_SENDER_NAME));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addAttachment(attachmentFilename,
                    new ByteArrayResource(pdfBytes), "application/pdf");
            sender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
        }
    }

    private JavaMailSenderImpl buildSender(String host) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(Integer.parseInt(parameterService.getString(ParameterName.MAIL_PORT)));
        sender.setUsername(parameterService.getString(ParameterName.MAIL_USER));
        sender.setPassword(parameterService.getString(ParameterName.MAIL_PASS));
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable",
                parameterService.getString(ParameterName.MAIL_SMTP_STARTTLS));
        return sender;
    }
}
