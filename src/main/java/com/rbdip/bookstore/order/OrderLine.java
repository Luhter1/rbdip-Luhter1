package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;

public record OrderLine(Product product, int quantity) {
}