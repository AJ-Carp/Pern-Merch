package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.entity.Address;
import com.ajcarpinello.Pern_Merch_Website.event.OrderPaidEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends transactional order emails over plain SMTP (provider-agnostic). The JavaMailSender
 * bean only exists when spring.mail.host is configured, so this is resolved lazily via
 * ObjectProvider — when mail isn't configured (e.g. local dev) sends are skipped, not errors.
 */
@Service
@Slf4j
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String merchantEmail;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${app.mail.from:}") String from,
                       @Value("${app.mail.merchant:}") String merchantEmail) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.merchantEmail = merchantEmail;
    }

    /**
     * Fires after the finalizeOrder transaction commits (@TransactionalEventListener) on a
     * separate thread (@Async) so the webhook thread is freed immediately and not held up
     * by the SMTP round-trip. Each send is isolated so one failure doesn't block the other.
     */
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        try {
            sendOrderConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to send customer confirmation for order {}", event.getOrderId(), e);
        }
        try {
            sendMerchantNotification(event);
        } catch (Exception e) {
            log.error("Failed to send merchant notification for order {}", event.getOrderId(), e);
        }
    }

    /** Receipt to the customer. */
    public void sendOrderConfirmation(OrderPaidEvent order) {
        String body = "Thanks for your order!\n\n"
                + orderSummary(order);
        send(order.getCustomerEmail(), "Order #" + order.getOrderId() + " confirmed", body);
    }

    /** New-order alert to the merchant so they know to fulfill it. */
    public void sendMerchantNotification(OrderPaidEvent order) {
        if (isBlank(merchantEmail)) {
            log.warn("app.mail.merchant not set — skipping merchant notification for order {}", order.getOrderId());
            return;
        }
        String body = "New order received.\n\n"
                + "Customer: " + order.getCustomerEmail() + "\n\n"
                + orderSummary(order);
        send(merchantEmail, "New order #" + order.getOrderId(), body);
    }

    private void send(String to, String subject, String body) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || isBlank(from) || isBlank(to)) {
            log.info("Mail not configured (sender/from/to missing) — skipping '{}' to {}", subject, to);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        sender.send(msg);
        log.info("Sent '{}' to {}", subject, to);
    }

    private String orderSummary(OrderPaidEvent order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(order.getOrderId()).append("\n\n");
        for (OrderPaidEvent.Line line : order.getItems()) {
            sb.append(String.format("  %s (%s) x%d — $%s%n",
                    line.getProductName(), line.getSize(), line.getQuantity(), line.getPrice()));
        }
        sb.append("\nTotal: $").append(order.getTotalAmount()).append("\n");

        Address a = order.getShippingAddress();
        if (a != null) {
            sb.append("\nShipping to:\n");
            sb.append("  ").append(a.getRecipientName()).append("\n");
            sb.append("  ").append(a.getLine1()).append("\n");
            if (!isBlank(a.getLine2())) sb.append("  ").append(a.getLine2()).append("\n");
            sb.append("  ").append(a.getCity()).append(", ").append(a.getState())
                    .append(" ").append(a.getPostalCode()).append("\n");
            sb.append("  ").append(a.getCountry()).append("\n");
        }
        return sb.toString();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
