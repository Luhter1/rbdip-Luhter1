package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPersistenceService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;

    public OrderPersistenceService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Order saveOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Customer customer = customerRepository.save(new Customer(
                request.customerFullName(),
                request.customerAddress(),
                request.customerPhone()));

        Order order = orderRepository.save(new Order(customer, "new"));

        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(
                    order.getId(),
                    line.product(),
                    line.quantity()));
        }

        return order;
    }
}