create database ao_phong_4p
use ao_phong_4p

use master
drop database ao_phong_4p

select * from product
select * from product_detail
select * from cart
select * from cart_detail
select * from bill
select * from bill_detail 
select * from brand
select * from category
select * from material
select * from size
select * from color
select * from image


select image_id from product_detail where image_id = 5 
select * from cart_detail where cart_id = 1

SELECT * FROM product_detail WHERE product_id = 1

SELECT SUM(quantity) FROM product_detail WHERE product_id = 3
delete from category where id = 3

-- Bảng role
CREATE TABLE role (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(100)
);

-- Bảng customer
CREATE TABLE customer (
   id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
	name NVARCHAR(100),
    phone_number NVARCHAR(20),
    email NVARCHAR(100)
);

-- Bảng account
CREATE TABLE account (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    full_name NVARCHAR(100),
    email NVARCHAR(100),
    password NVARCHAR(100),
    birth_of_date DATE,
    phone_number NVARCHAR(20),
    gender NVARCHAR(10),
	status BIT,
    role_id INT FOREIGN KEY REFERENCES role(id),
	customer_id INT FOREIGN KEY REFERENCES customer(id)
);



-- Bảng payment_method
CREATE TABLE payment_method (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(100)
);

-- Bảng discount
CREATE TABLE discount (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    code_name NVARCHAR(100),
    discount_type NVARCHAR(20),
    discount_value DECIMAL(10, 2),
    start_datetime DATETIME,
    end_datetime DATETIME,
    min_purchase DECIMAL(10, 2),
    max_discount DECIMAL(10, 2),
    usage_limit INT,
    description NVARCHAR(200),
    status INT
);


-- Bảng bill
CREATE TABLE bill (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    discount_amount DECIMAL(10, 2),
    total_amount DECIMAL(10, 2),
    payment_status BIT,   --sửa thành bit
    status INT, --sửa thành INT
	type_bill BIT, --thêm type phân loại 2 loại bill
    delivery_type NVARCHAR(50),
    shipping_fee DECIMAL(10, 2),
	name NVARCHAR(100),
    phone_number NVARCHAR(20),
    email NVARCHAR(100),
	created_at DATETIME, --thêm ngày tạo
	updated_at DATETIME, --thêm ngày sửa
    payment_method_id INT FOREIGN KEY REFERENCES payment_method(id),
    discount_id INT FOREIGN KEY REFERENCES discount(id),
    customer_id INT FOREIGN KEY REFERENCES customer(id),
);

-- Bảng bill_history
CREATE TABLE bill_history (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    status NVARCHAR(50),
    bill_id INT FOREIGN KEY REFERENCES bill(id),
    account_id INT FOREIGN KEY REFERENCES account(id)
);

-- Bảng shipping_address
CREATE TABLE shipping_address (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    address_detail NVARCHAR(200),
    phone_number NVARCHAR(20),
    is_default BIT,
    customer_id INT FOREIGN KEY REFERENCES customer(id)
);

-- Bảng category
CREATE TABLE category (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(100),
    description NVARCHAR(200),
    status BIT
);

-- Bảng brand
CREATE TABLE brand (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(100),
    status BIT
);

-- Bảng material
CREATE TABLE material (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(100),
    status BIT
);

-- Bảng product
CREATE TABLE product (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(100),
    status BIT,
    description NVARCHAR(200),
    category_id INT FOREIGN KEY REFERENCES category(id),
    brand_id INT FOREIGN KEY REFERENCES brand(id),
    material_id INT FOREIGN KEY REFERENCES material(id)
);

-- Bảng color
CREATE TABLE color (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(100),
    status BIT
);

-- Bảng size
CREATE TABLE size (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code NVARCHAR(50),
    name NVARCHAR(50),
    status BIT
);

-- Bảng image
CREATE TABLE image (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(100),
    path_file NVARCHAR(200),
);

-- Bảng product_detail
CREATE TABLE product_detail (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    price DECIMAL(10, 2),
    quantity INT,
    barcode NVARCHAR(100),
    product_id INT FOREIGN KEY REFERENCES product(id),
    color_id INT FOREIGN KEY REFERENCES color(id),
    size_id INT FOREIGN KEY REFERENCES size(id),
    image_id INT FOREIGN KEY REFERENCES image(id)
);


-- Bảng bill_detail
CREATE TABLE bill_detail (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	price DECIMAL(10, 2), -- thêm price
    quantity INT, -- thêm quantity
	total_price DECIMAL(10, 2), -- thêm total_price
    bill_id INT FOREIGN KEY REFERENCES bill(id),
    product_detail_id INT FOREIGN KEY REFERENCES product_detail(id)
);

-- Bảng cart (giỏ hàng chính)
CREATE TABLE cart (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	total_price_cart DECIMAL(10, 2),
	total_quantity INT,
    created_at DATE DEFAULT GETDATE(),
    updated_at DATE DEFAULT GETDATE(),
    status BIT,
    account_id INT FOREIGN KEY REFERENCES account(id),
);


-- Bảng cart_detail
CREATE TABLE cart_detail (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    price DECIMAL(10, 2),
    quantity INT,
	total_price DECIMAL(10, 2),
    --total_price AS (price * quantity) PERSISTED,
    --status NVARCHAR(50),
    cart_id INT FOREIGN KEY REFERENCES cart(id),
    product_detail_id INT FOREIGN KEY REFERENCES product_detail(id)
);



-- Bảng category
INSERT INTO category (name, description, status) VALUES 
(N'Áo', N'Áo thun, áo sơ mi', 1),
(N'Áo polo',N'Áo polo, áo polo', 1)

-- Bảng brand
INSERT INTO brand (code, name, status) VALUES 
('b1', 'Nike', 1),
('b2', 'Adidas', 1);

-- Bảng material
INSERT INTO material (code, name, status) VALUES 
('m1', 'Cotton', 1),
('m2', 'Polyester', 1);

-- Bảng color
INSERT INTO color (code, name, status) VALUES 
('COL1', N'Đỏ', 1),
('COL2', N'Xanh dương', 1);

-- Bảng size
INSERT INTO size (code, name, status) VALUES 
('S', 'Small', 1),
('M', 'Medium', 1);

-- Bảng product
INSERT INTO product (code, name, status, description, category_id, brand_id, material_id) VALUES 
('PROD1', N'Áo thun Nike', 1, N'Áo thun thể thao nam', 1, 1, 1),
('PROD2', N'Quần Adidas', 1, N'Áo polo nam', 2, 2, 2);

-- Bảng image
INSERT INTO image (name, path_file) VALUES 
('ao_nike.jpg', '/images/ao_nike.jpg'),
('quan_adidas.jpg', '/images/quan_adidas.jpg');

-- Bảng product_detail
INSERT INTO product_detail (price, quantity, barcode, product_id, color_id, size_id, image_id) VALUES 
(250000, 50, '1234567890123', 1, 1, 1, 1),  -- Áo Nike đỏ size S
(350000, 30, '1234567890456', 2, 2, 2, 2); -- áo adidas xanh


-- Bảng shipping_address
INSERT INTO shipping_address (address_detail, phone_number, is_default, customer_id) VALUES 
(N'123 Lê Lợi, Hà Nội', '0909123456', 1, 1),
(N'456 Trần Phú, Đà Nẵng', '0938123456', 0, 2);


-- Bảng role
INSERT INTO role (name) VALUES 
(N'Admin'),
(N'Employee'),
(N'Customer');

-- Bảng account
INSERT INTO account (code, full_name, email, password, birth_of_date, phone_number, gender, role_id) VALUES 
('ACC001', N'Nguyễn Văn A', 'a@example.com', '123456', '1990-01-01', '0987654321', N'Nam', 1),
('ACC002', N'Trần Thị B', 'b@example.com', '123456', '1995-05-05', '0912345678', N'Nữ', 2);

-- Bảng customer
INSERT INTO customer (code, account_id) VALUES 
('CUS001', 1),
('CUS002', 2);

-- Bảng discount
INSERT INTO discount (code, code_name, discount_type, discount_value, start_datetime, end_datetime, min_purchase, max_discount, usage_limit, description, status) VALUES 
('SALE10', N'Giảm 10%', 'PERCENT', 10.00, '2023-11-01', '2023-12-31', 500000, 100000, 100, N'Giảm 10% cho đơn hàng từ 500k', 1),
('TET30', N'Giảm Tết 30k', 'FIXED', 30000.00, '2023-11-01', '2023-12-31', 0, 30000, NULL, N'Giảm 30,000', 1);

-- Bảng payment_method
INSERT INTO payment_method (name) VALUES 
(N'Tiền mặt'),
(N'Chuyển khoản');
