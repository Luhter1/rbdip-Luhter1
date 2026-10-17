package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderLineAssembler {

    private final ProductRepository productRepository;

    public OrderLineAssembler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderLine> assemble(CreateOrderRequest request) {
        List<OrderLine> lines = new ArrayList<>();

        for (CreateOrderRequest.Item raw : request.items()) {
            Product product = productRepository.findById(raw.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "product " + raw.productId() + " not found"));

            int quantity = raw.quantity() == null ? 1 : raw.quantity();
            lines.add(new OrderLine(product, quantity));
        }

        return lines;
    }
}