-- Foodie Admin – MySQL reference schema.
-- You do NOT have to run this: Spring Boot creates/updates these tables itself
-- (spring.jpa.hibernate.ddl-auto=update). It is here so you can see / recreate the structure.

CREATE DATABASE IF NOT EXISTS foodie_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE foodie_admin;

CREATE TABLE IF NOT EXISTS admins (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(60)  NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL            -- BCrypt
);

CREATE TABLE IF NOT EXISTS seller_applications (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(100),
  restaurant_name VARCHAR(255) NOT NULL,
  owner_name VARCHAR(255) NOT NULL,
  cuisine VARCHAR(255) NOT NULL,
  phone VARCHAR(255) NOT NULL,
  address VARCHAR(1000) NOT NULL,
  city VARCHAR(255) NOT NULL,
  fssai_number VARCHAR(255) NOT NULL,
  image VARCHAR(1000),
  fssai_document_file_name VARCHAR(255),
  status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
  review_note VARCHAR(1000),
  seller_id VARCHAR(255),
  submitted_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS rider_applications (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(100),
  name VARCHAR(255) NOT NULL,
  phone VARCHAR(255) NOT NULL,
  vehicle VARCHAR(255) NOT NULL,
  vehicle_number VARCHAR(255) NOT NULL,
  license_number VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  photo_file_name VARCHAR(255),
  license_file_name VARCHAR(255),
  status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
  review_note VARCHAR(1000),
  rider_id VARCHAR(255),
  submitted_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS portal_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(30) NOT NULL,
  display_name VARCHAR(100),
  profile_id VARCHAR(40),
  phone VARCHAR(40),
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS sellers (
  id VARCHAR(20) NOT NULL PRIMARY KEY,
  name VARCHAR(255), owner_name VARCHAR(255), owner_avatar VARCHAR(1000), image VARCHAR(1000),
  cuisine VARCHAR(255), phone VARCHAR(255), email VARCHAR(255), address VARCHAR(255),
  city VARCHAR(255), fssai_number VARCHAR(255),
  rating DOUBLE NOT NULL, status VARCHAR(255), status_note VARCHAR(1000),
  is_open BIT NOT NULL, joined_on VARCHAR(255),
  total_orders INT NOT NULL, revenue DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS seller_menu_items (
  seller_id VARCHAR(20) NOT NULL, name VARCHAR(255), category VARCHAR(255),
  price DOUBLE NOT NULL, veg BIT NOT NULL, image VARCHAR(255),
  FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS riders (
  id VARCHAR(20) NOT NULL PRIMARY KEY,
  name VARCHAR(255), avatar VARCHAR(1000), phone VARCHAR(255), email VARCHAR(255),
  vehicle VARCHAR(255), vehicle_number VARCHAR(255), license_number VARCHAR(255),
  city VARCHAR(255), status VARCHAR(255), status_note VARCHAR(1000), rating DOUBLE NOT NULL,
  total_deliveries INT NOT NULL, earnings DOUBLE NOT NULL, joined_on VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS rider_documents (
  rider_id VARCHAR(20) NOT NULL, type VARCHAR(255), status VARCHAR(255),
  FOREIGN KEY (rider_id) REFERENCES riders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS food_orders (
  id VARCHAR(20) NOT NULL PRIMARY KEY,
  customer_id VARCHAR(40), customer_name VARCHAR(255),
  seller_id VARCHAR(20), seller_name VARCHAR(255),
  rider_id VARCHAR(20), rider_name VARCHAR(255),
  status VARCHAR(40) NOT NULL, amount DOUBLE NOT NULL,
  payment VARCHAR(40), delivery_address VARCHAR(500),
  created_at TIMESTAMP(6), updated_at TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS food_order_items (
  order_id VARCHAR(20) NOT NULL, name VARCHAR(255),
  price DOUBLE NOT NULL, quantity INT NOT NULL,
  FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE
);
