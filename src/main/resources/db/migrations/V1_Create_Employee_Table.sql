CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NULL,
    password VARCHAR(255) NOT NULL,
    password_strength_value INT NOT NULL,
    dependent_id BIGINT NULL,
    FOREIGN KEY (dependent_id) REFERENCES employees(id)
);