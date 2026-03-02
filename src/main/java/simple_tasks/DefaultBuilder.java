package simple_tasks;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DefaultBuilder {

    static void main() {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .cardNumber("4111").amount(BigDecimal.valueOf(100.00)).currency("USD")
                .build();

        PaymentRequest updatedPaymentRequest = paymentRequest.toBuilder().amount(BigDecimal.valueOf(200.00)).build();

        System.out.println(paymentRequest.createdAt);
        System.out.println(updatedPaymentRequest.createdAt);
    }

    @Value
    @Builder(toBuilder = true)
    static class PaymentRequest {
        String cardNumber;
        BigDecimal amount;
        String currency;

        @Builder.Default
        LocalDateTime createdAt = LocalDateTime.now();
    }

}
