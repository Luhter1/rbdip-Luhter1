package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Характеризационные тесты PricingCalculator (ЛР2): фиксируют текущее
 * поведение ветвей, которые эталонный тест намеренно не покрывает
 * (скидка за объём, VIP/wholesale, купоны, кламп отрицательного итога,
 * потолок цены, округление).
 */
class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    private static PricingCalculator.LineItem item(String price, int quantity) {
        return new PricingCalculator.LineItem(new BigDecimal(price), quantity);
    }

    @Test
    void quantityExactlyTenGetsNoBulkDiscount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("10.00", 10)), "regular", null))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void quantityAboveTenGetsFivePercentBulkDiscount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("10.00", 11)), "regular", null))
                .isEqualByComparingTo("104.50");
    }

    @Test
    void vipCustomerGetsTenPercentDiscount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "vip", null))
                .isEqualByComparingTo("90.00");
    }

    @Test
    void wholesaleCustomerGetsFifteenPercentDiscount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "wholesale", null))
                .isEqualByComparingTo("85.00");
    }

    @Test
    void unknownCustomerTypeIsTreatedAsRegular() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "premium", null))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void save10CouponSubtractsFixedAmount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "regular", "SAVE10"))
                .isEqualByComparingTo("90.00");
    }

    @Test
    void save20PercentCouponAppliesPercentageDiscount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "regular", "SAVE20PERCENT"))
                .isEqualByComparingTo("80.00");
    }

    @Test
    void unknownCouponCodeIsIgnored() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "regular", "SUMMER"))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void couponDrivingTotalNegativeIsClampedToZero() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("5.00", 1)), "regular", "SAVE10"))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void totalExactlyAtPriceCapIsNotDiscounted() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("1000.00", 1)), "regular", null))
                .isEqualByComparingTo("1000.00");
    }

    @Test
    void totalAbovePriceCapGetsTwoPercentDiscount() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("1000.01", 1)), "regular", null))
                .isEqualByComparingTo("980.01");
    }

    @Test
    void multipleItemsAreSummedBeforeDiscounts() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("10.00", 2), item("20.00", 3)), "regular", null))
                .isEqualByComparingTo("80.00");
    }

    @Test
    void nullCouponCodeIsIgnored() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "regular", null))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void wholesaleDiscountRoundsToTwoDecimalsHalfUp() {
        assertThat(calculator.calculateOrderTotal(
                List.of(item("0.07", 1)), "wholesale", null))
                .isEqualByComparingTo("0.06");
    }

    @Test
    void vipAndCouponDiscountsCompose() {
        // 100 * 0.9 = 90; затем SAVE10 вычитает 10.
        assertThat(calculator.calculateOrderTotal(
                List.of(item("100.00", 1)), "vip", "SAVE10"))
                .isEqualByComparingTo("80.00");
    }

    @Test
    void calculateTotalDefaultsNullCustomerTypeToRegular() {
        Product product = new Product("Book", new BigDecimal("100.00"), null);
        BigDecimal result = calculator.calculateTotal(
                List.of(new OrderLine(product, 1)), null, null);

        assertThat(result).isEqualByComparingTo("100.00");
    }

    @Test
    void calculateTotalDelegatesToOrderTotalForVip() {
        Product product = new Product("Book", new BigDecimal("100.00"), null);
        BigDecimal result = calculator.calculateTotal(
                List.of(new OrderLine(product, 1)), "vip", null);

        assertThat(result).isEqualByComparingTo("90.00");
    }
}