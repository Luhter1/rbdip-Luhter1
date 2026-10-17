package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Модуль расчёта цены заказа. Намеренно почти не покрыт тестами и
 * содержит magic numbers / нечитаемые ветвления скидок - цель для
 * характеризационных тестов (ЛР2) и mutation-testing гейта PIT (ЛР5).
 */
public class PricingCalculator {    
    private static final double DISCOUNT_MIN_QUANTITY_10  = 10;
    private static final BigDecimal DISCOUNT_MIN_QUANTITY_10_MULTIPLIER  = new BigDecimal("0.95");

    private static final BigDecimal DISCOUNT_VIP_CUSTOMER = new BigDecimal("0.9");
    private static final BigDecimal DISCOUNT_WHOLESALE = new BigDecimal("0.85");
    

    private static final BigDecimal DISCOUNT_PROMO_CODE_SAVE20PERCENT = new BigDecimal("0.8");
    private static final BigDecimal DISCOUNT_PROMO_CODE_SAVE10 = BigDecimal.TEN;

    private static final BigDecimal DISCOUNT_MIN_PRICE_1000  = new BigDecimal("1000");
    private static final BigDecimal DISCOUNT_MIN_PRICE_1000_MULTIPLIER  = new BigDecimal("0.98");

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = BigDecimal.ZERO;

        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > DISCOUNT_MIN_QUANTITY_10) {
                linePrice = linePrice.multiply(DISCOUNT_MIN_QUANTITY_10_MULTIPLIER);
            }
            total = total.add(linePrice);
        }

        if ("vip".equals(customerType)) {
            total = total.multiply(DISCOUNT_VIP_CUSTOMER);
        } else if ("wholesale".equals(customerType)) {
            total = total.multiply(DISCOUNT_WHOLESALE);
        }

        if ("SAVE10".equals(couponCode)) {
            total = total.subtract(DISCOUNT_PROMO_CODE_SAVE10);
        } else if ("SAVE20PERCENT".equals(couponCode)) {
            total = total.multiply(DISCOUNT_PROMO_CODE_SAVE20PERCENT);
        }

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        if (total.compareTo(DISCOUNT_MIN_PRICE_1000) > 0) {
            total = total.multiply(DISCOUNT_MIN_PRICE_1000_MULTIPLIER);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(
            List<OrderLine> lines,
            String customerType,
            String couponCode) {

        List<PricingCalculator.LineItem> lineItems = lines.stream()
                .map(line -> new PricingCalculator.LineItem(
                        line.product().getPrice(),
                        line.quantity()))
                .collect(Collectors.toList());

        return calculateOrderTotal(
                lineItems,
                customerType == null ? "regular" : customerType,
                couponCode);
    }
}
