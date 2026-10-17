package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPersistenceService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderPersistenceService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order saveOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Order order = new Order(
                request.customerFullName(),
                request.customerAddress(),
                request.customerPhone(),
                "new");

        order = orderRepository.save(order);

        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(
                    order.getId(),
                    line.product().getName(),
                    line.product().getPrice(),
                    line.quantity()));
        }

        return order;
    }
}