-- Initialize additional databases for the stack
-- The default MySQL service already creates the `invoices` database via MYSQL_DATABASE
-- Here we ensure the `mappings` database exists as well
CREATE DATABASE IF NOT EXISTS `mappings` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Optionally, you can create dedicated users (commented out). The services currently use root.
-- CREATE USER IF NOT EXISTS 'mappings'@'%' IDENTIFIED BY 'mappings';
-- GRANT ALL PRIVILEGES ON `mappings`.* TO 'mappings'@'%';
-- CREATE USER IF NOT EXISTS 'invoices'@'%' IDENTIFIED BY 'invoices';
-- GRANT ALL PRIVILEGES ON `invoices`.* TO 'invoices'@'%';
-- FLUSH PRIVILEGES;
