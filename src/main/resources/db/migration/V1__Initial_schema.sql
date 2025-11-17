-- Catalog schema
-- Main products table plus supporting collections for images, categories and filters

CREATE TABLE products (
    id UUID PRIMARY KEY,
    site_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(25) NOT NULL,
    scheduled_publish_at TIMESTAMP NULL,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_site ON products(site_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_site_status ON products(site_id, status);

CREATE TABLE product_images (
    product_id UUID NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    PRIMARY KEY (product_id, image_url),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE product_categories (
    product_id UUID NOT NULL,
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, category),
    CONSTRAINT fk_product_categories_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE product_filters (
    id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL,
    filter_key VARCHAR(100) NOT NULL,
    filter_type VARCHAR(25) NOT NULL,
    text_value VARCHAR(255),
    numeric_value NUMERIC(19,2),
    min_value NUMERIC(19,2),
    max_value NUMERIC(19,2),
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL,
    unit VARCHAR(25),
    CONSTRAINT fk_product_filters_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_filters_key ON product_filters(filter_key);