package dev.beanguard.server.services;

import dev.beanguard.api.models.licence.LicenceType;
import dev.beanguard.api.models.shop.Order;
import java.time.Instant;
import java.util.UUID;

public interface MailService {
    void sendDemoWelcome(String to, String licenceToken, Instant expiration);
    void sendExpiryWarning(String to, String companyName, Instant expiration, int daysLeft, LicenceType type);
    void sendTransferConfirmation(String to, String confirmUrl);
    void sendLicenceCreated(String to, UUID licenceKey);
    void sendLicenceCreated(String to, UUID licenceKey, byte[] certificatePdf);
    void sendOrderConfirmation(String to, Order order, byte[] proFormaPdf, String proFormaNumber);
    void sendOrderCancellation(String to, Order order);
}
