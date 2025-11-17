-- Categories table and product-category relationship

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    site_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_per_site UNIQUE (site_id, name)
);

DROP TABLE IF EXISTS product_categories;

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

