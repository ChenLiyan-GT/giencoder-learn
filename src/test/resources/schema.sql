CREATE TABLE IF NOT EXISTS scash.m101_company (
    company_id          BIGSERIAL       PRIMARY KEY,
    company_cd          VARCHAR(20)     NOT NULL,
    company_nm_kana     VARCHAR(40),
    company_nm_kanji    VARCHAR(40)     NOT NULL,
    company_abbreviation VARCHAR(20),
    postal_cd           VARCHAR(7),
    area_cd             VARCHAR(5),
    address             VARCHAR(240),
    phone_no            VARCHAR(20),
    fax_no              VARCHAR(20),
    created_ts          TIMESTAMP,
    created_user_cd     VARCHAR(16),
    created_program     VARCHAR(50),
    updated_ts          TIMESTAMP,
    updated_user_cd     VARCHAR(16),
    updated_program     VARCHAR(50),
    version             INTEGER         NOT NULL DEFAULT 0,
    deleted_flag        VARCHAR(1)      NOT NULL DEFAULT '0'
);

CREATE TABLE IF NOT EXISTS scash.a101_product (
    product_id          BIGSERIAL       PRIMARY KEY,
    product_cd          VARCHAR(20)     NOT NULL UNIQUE,
    product_nm_kanji    VARCHAR(40)     NOT NULL,
    product_nm_kana     VARCHAR(40),
    unit_cd             VARCHAR(10),
    created_ts          TIMESTAMP,
    created_user_cd     VARCHAR(16),
    created_program     VARCHAR(50),
    updated_ts          TIMESTAMP,
    updated_user_cd     VARCHAR(16),
    updated_program     VARCHAR(50),
    version             INTEGER         NOT NULL DEFAULT 0,
    deleted_flag        VARCHAR(1)      NOT NULL DEFAULT '0'
);

CREATE TABLE IF NOT EXISTS scash.a102_inventory (
    inventory_id        BIGSERIAL       PRIMARY KEY,
    company_cd          VARCHAR(20)     NOT NULL,
    product_cd          VARCHAR(20)     NOT NULL,
    quantity            INTEGER         NOT NULL DEFAULT 0,
    reserved_quantity   INTEGER         NOT NULL DEFAULT 0,
    created_ts          TIMESTAMP,
    created_user_cd     VARCHAR(16),
    created_program     VARCHAR(50),
    updated_ts          TIMESTAMP,
    updated_user_cd     VARCHAR(16),
    updated_program     VARCHAR(50),
    version             INTEGER         NOT NULL DEFAULT 0,
    deleted_flag        VARCHAR(1)      NOT NULL DEFAULT '0'
);
