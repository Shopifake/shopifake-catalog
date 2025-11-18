-- Catalog schema
-- Consolidated baseline for products, categories, filters and supporting tables

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

-- Categories
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    site_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_per_site UNIQUE (site_id, name)
);

CREATE TABLE product_categories (
    product_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_categories_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_categories_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_categories_site ON categories(site_id);
CREATE INDEX idx_product_categories_category ON product_categories(category_id);

-- Filters
CREATE TABLE filters (
    id UUID PRIMARY KEY,
    site_id UUID NOT NULL,
    filter_key VARCHAR(100) NOT NULL,
    filter_type VARCHAR(25) NOT NULL,
    display_name VARCHAR(255),
    unit VARCHAR(25),
    min_value NUMERIC(19,2),
    max_value NUMERIC(19,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_filter_key_per_site UNIQUE (site_id, filter_key)
);

CREATE TABLE filter_values (
    filter_id UUID NOT NULL,
    value_text VARCHAR(255) NOT NULL,
    PRIMARY KEY (filter_id, value_text),
    CONSTRAINT fk_filter_values_filter FOREIGN KEY (filter_id)
        REFERENCES filters(id) ON DELETE CASCADE
);

CREATE INDEX idx_filters_site ON filters(site_id);
CREATE INDEX idx_filters_key ON filters(filter_key);

-- Product filters referencing reusable filter definitions
CREATE TABLE product_filters (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    filter_id UUID NOT NULL,
    text_value VARCHAR(255),
    numeric_value NUMERIC(19,2),
    min_value NUMERIC(19,2),
    max_value NUMERIC(19,2),
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL,
    CONSTRAINT fk_product_filters_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_filters_filter FOREIGN KEY (filter_id)
        REFERENCES filters(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_filters_product ON product_filters(product_id);
CREATE INDEX idx_product_filters_filter ON product_filters(filter_id);