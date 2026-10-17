package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * God-класс: валидация, расчёт цены, персистентность и "уведомление
 * клиента" смешаны в одном методе. Цель для рефакторинга по SRP в ЛР1.
 */
@Service
public class OrderService {

    private final OrderLineAssembler orderLineAssembler;
    private final OrderPersistenceService orderPersistenceService;
    private final PricingCalculator pricingCalculator = new PricingCalculator();
    private final OrderNotificationService orderNotificationService = new OrderNotificationService();

    public OrderService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.orderLineAssembler = new OrderLineAssembler(productRepository);
        this.orderPersistenceService = new OrderPersistenceService(
                orderRepository, 
                orderItemRepository);
    }

    public void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.customerFullName() == null || request.customerFullName().isBlank()) {
            throw new IllegalArgumentException("customerFullName is required");
        }
        if (request.customerAddress() == null || request.customerAddress().isBlank()) {
            throw new IllegalArgumentException("customerAddress is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }

        for (CreateOrderRequest.Item item : request.items()) {
            if (item == null || item.productId() == null) {
                throw new IllegalArgumentException("productId is required");
            }
            if (item.quantity() != null && item.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        validate(request);

        List<OrderLine> lines = orderLineAssembler.assemble(request);

        BigDecimal total = pricingCalculator.calculateTotal(
                lines,
                request.customerType(),
                request.couponCode());

        Order order = orderPersistenceService.saveOrder(request, lines);

        orderNotificationService.sendConfirmationEmail(
                request.customerFullName(), 
                order.getId(), 
                total);

        return order;
    }
}
