-- ЛР2: нормализация схемы.
-- Контактные данные клиента выносятся из orders в отдельную таблицу
-- customers (имя клиента - в колонку full_name), а данные товара из
-- order_items - в ссылку на products (product_id). Дублирующиеся
-- колонки удаляются.

-- 1. Таблица клиентов.
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

-- Перенос существующих клиентов: одна строка на имя (собираем из текущих
-- заказов). DISTINCT ON оставляет один набор контактов на клиента.
INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT ON (customer_full_name) customer_full_name, customer_address, customer_phone
FROM orders
ORDER BY customer_full_name;

-- 2. orders -> customers.
ALTER TABLE orders ADD COLUMN customer_id BIGINT;

UPDATE orders o
SET customer_id = c.id
FROM customers c
WHERE o.customer_full_name = c.full_name;

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE orders DROP COLUMN customer_full_name;
ALTER TABLE orders DROP COLUMN customer_address;
ALTER TABLE orders DROP COLUMN customer_phone;

-- 3. order_items -> products.
ALTER TABLE order_items ADD COLUMN product_id BIGINT;

UPDATE order_items oi
SET product_id = p.id
FROM products p
WHERE oi.product_name = p.name;

ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products(id);

ALTER TABLE order_items DROP COLUMN product_name;
ALTER TABLE order_items DROP COLUMN product_price;