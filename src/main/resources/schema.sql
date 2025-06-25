-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:4306
-- Generation Time: Jun 15, 2025 at 10:31 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `uiuonepoint`
--

-- --------------------------------------------------------

--
-- Table structure for table `eshopping_orders`
--

CREATE TABLE `eshopping_orders` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `mobile_number` varchar(15) NOT NULL,
  `status` enum('pending','received','on_campus','delivered','canceled') NOT NULL DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `eshopping_orders`
--

INSERT INTO `eshopping_orders` (`id`, `user_id`, `product_id`, `quantity`, `mobile_number`, `status`, `created_at`, `updated_at`) VALUES
(1, 3, 1, 2, '018123456', 'pending', '2025-06-01 19:52:16', '2025-06-01 19:52:16');

-- --------------------------------------------------------

--
-- Table structure for table `eshopping_products`
--

CREATE TABLE `eshopping_products` (
  `id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int(11) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `eshopping_products`
--

INSERT INTO `eshopping_products` (`id`, `seller_id`, `name`, `description`, `price`, `stock_quantity`, `category`, `image_path`, `created_at`) VALUES
(1, 2, 'T-shirt', 'Pink t-shirt. Available in S, L, XL, XXL, XXXL.', 450.00, 10, 'Clothing', 'C:\\Users\\USER\\Desktop\\t-shirt-mockup-design_23-2151947.jpg', '2025-06-01 19:28:36');

-- --------------------------------------------------------

--
-- Table structure for table `food_items`
--

CREATE TABLE `food_items` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(20) NOT NULL,
  `available` tinyint(1) DEFAULT 1,
  `image` varchar(255) DEFAULT NULL,
  `stock_quantity` int(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `food_items`
--

INSERT INTO `food_items` (`id`, `name`, `price`, `description`, `category`, `available`, `image`, `stock_quantity`) VALUES
(1, 'Sandwitch', 80.00, 'Just a sandwitch', 'snacks', 1, 'https:\\/\\/i.ibb.co.com\\/fd1yQrPC\\/5807a9eff88c.jpg', 6),
(2, 'French Fries', 80.00, 'Crispy potato fries', 'snacks', 1, 'https:\\/\\/i.ibb.co.com\\/hJkrkj3x\\/2bfe16297420.jpg', 4),
(3, 'Coffee', 40.00, 'Hot brewed coffee', 'breakfast', 1, 'https:\\/\\/i.ibb.co.com\\/x8gzrd2G\\/5520f6580077.jpg', 3),
(4, 'Beef Curry', 250.00, 'Classic Beef Curry', 'lunch', 1, 'https:\\/\\/i.ibb.co.com\\/CKKPdHtj\\/820eee32f42a.jpg', 9),
(5, 'Chicken Fry', 180.00, 'Kentucky Fried Chicken', 'snacks', 1, 'https:\\/\\/i.ibb.co.com\\/fV13qmjs\\/cb9efed4999a.jpg', 5),
(6, 'Duck fry', 300.00, 'Cool duck fry with extra sauce', 'lunch', 1, 'https:\\/\\/i.ibb.co\\/j9qfTrRf\\/fd1cb1826d12.jpg', 7),
(7, 'Mojo', 20.00, 'Mojo', 'snacks', 1, 'https:\\/\\/i.ibb.co\\/3mwcKCxW\\/2d0c3f690ad5.jpg', 94);

-- --------------------------------------------------------

--
-- Table structure for table `food_orders`
--

CREATE TABLE `food_orders` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `order_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_amount` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `food_orders`
--

INSERT INTO `food_orders` (`id`, `user_id`, `status`, `order_time`, `total_amount`) VALUES
(1, 3, 'completed', '2025-04-27 14:29:01', 220.00),
(2, 3, 'completed', '2025-04-27 15:20:46', 80.00),
(3, 3, 'completed', '2025-04-27 17:20:07', 180.00),
(4, 2, 'completed', '2025-04-27 17:58:06', 430.00),
(5, 2, 'cancelled', '2025-05-05 17:37:58', 460.00),
(6, 3, 'completed', '2025-05-05 19:03:19', 420.00),
(7, 3, 'cancelled', '2025-05-08 08:42:23', 220.00),
(8, 3, 'completed', '2025-05-27 14:55:33', 260.00),
(9, 3, 'cancelled', '2025-05-27 15:21:24', 310.00),
(10, 3, 'cancelled', '2025-05-27 15:55:20', 120.00),
(11, 3, 'cancelled', '2025-05-29 07:10:27', 740.00),
(12, 3, 'cancelled', '2025-05-30 16:53:07', 120.00),
(13, 3, 'completed', '2025-05-30 17:28:22', 40.00),
(14, 3, 'cancelled', '2025-06-01 09:44:24', 180.00);

-- --------------------------------------------------------

--
-- Table structure for table `food_order_items`
--

CREATE TABLE `food_order_items` (
  `id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `food_item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `food_order_items`
--

INSERT INTO `food_order_items` (`id`, `order_id`, `food_item_id`, `quantity`, `unit_price`) VALUES
(1, 1, 1, 1, 180.00),
(2, 1, 3, 1, 40.00),
(3, 2, 2, 1, 80.00),
(4, 3, 1, 1, 180.00),
(5, 4, 4, 1, 250.00),
(6, 4, 5, 1, 180.00),
(7, 5, 1, 1, 80.00),
(8, 5, 2, 1, 80.00),
(9, 5, 6, 1, 300.00),
(10, 6, 3, 1, 40.00),
(11, 6, 6, 1, 300.00),
(12, 6, 1, 1, 80.00),
(13, 7, 5, 1, 180.00),
(14, 7, 3, 1, 40.00),
(15, 8, 3, 2, 40.00),
(16, 8, 5, 1, 180.00),
(17, 9, 7, 3, 20.00),
(18, 9, 4, 1, 250.00),
(19, 10, 3, 1, 40.00),
(20, 10, 2, 1, 80.00),
(21, 11, 6, 1, 300.00),
(22, 11, 5, 2, 180.00),
(23, 11, 1, 1, 80.00),
(24, 12, 3, 1, 40.00),
(25, 12, 2, 1, 80.00),
(26, 13, 7, 2, 20.00),
(27, 14, 2, 2, 80.00),
(28, 14, 7, 1, 20.00);

-- --------------------------------------------------------

--
-- Table structure for table `grocery_items`
--

CREATE TABLE `grocery_items` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `category` varchar(20) NOT NULL,
  `stock_quantity` int(11) NOT NULL DEFAULT 0,
  `image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `grocery_items`
--

INSERT INTO `grocery_items` (`id`, `name`, `price`, `category`, `stock_quantity`, `image`) VALUES
(1, 'Pentonic Pen', 20.00, 'stationery', 6, 'https:\\/\\/i.ibb.co\\/zWXdMNZZ\\/8c906b653f4c.jpg'),
(2, 'Boshundhara Exercise Book', 60.00, 'stationery', 24, 'https:\\/\\/i.ibb.co\\/HT87mX24\\/b344ffe92d84.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `grocery_orders`
--

CREATE TABLE `grocery_orders` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `order_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_amount` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `grocery_orders`
--

INSERT INTO `grocery_orders` (`id`, `user_id`, `status`, `order_time`, `total_amount`) VALUES
(1, 3, 'completed', '2025-04-27 17:20:24', 20.00),
(2, 2, 'cancelled', '2025-04-27 17:58:30', 20.00),
(3, 2, 'completed', '2025-05-05 17:36:57', 100.00);

-- --------------------------------------------------------

--
-- Table structure for table `grocery_order_items`
--

CREATE TABLE `grocery_order_items` (
  `id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `grocery_item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `grocery_order_items`
--

INSERT INTO `grocery_order_items` (`id`, `order_id`, `grocery_item_id`, `quantity`, `unit_price`) VALUES
(1, 1, 1, 1, 20.00),
(2, 2, 1, 1, 20.00),
(3, 3, 2, 1, 60.00),
(4, 3, 1, 2, 20.00);

-- --------------------------------------------------------

--
-- Table structure for table `seller_requests`
--

CREATE TABLE `seller_requests` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `status` varchar(20) NOT NULL,
  `request_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `reason` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `seller_requests`
--

INSERT INTO `seller_requests` (`id`, `user_id`, `status`, `request_date`, `reason`) VALUES
(1, 3, 'rejected', '2025-05-27 15:44:31', 'I am small enterprenuer. i wanna increase my business'),
(2, 2, 'approved', '2025-05-29 07:12:00', 'i sell drugs');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`) VALUES
(1, 'admin', 'admin123', 'ADMIN', 'admin@uiuonepoint.com'),
(2, 'qwerty', '123456', 'SELLER', 'qwerty@gmail.com'),
(3, 'noman', 'nomani', 'CUSTOMER', 'noman@gmail.com'),
(6, 'customer1', 'pass123', 'CUSTOMER', 'customer1@uiu.ac.bd'),
(7, 'mahathir', 'mahathir', 'CUSTOMER', 'mmohammad2230889@gmail.com');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `eshopping_orders`
--
ALTER TABLE `eshopping_orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `eshopping_products`
--
ALTER TABLE `eshopping_products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `seller_id` (`seller_id`);

--
-- Indexes for table `food_items`
--
ALTER TABLE `food_items`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `food_orders`
--
ALTER TABLE `food_orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `food_order_items`
--
ALTER TABLE `food_order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `food_item_id` (`food_item_id`);

--
-- Indexes for table `grocery_items`
--
ALTER TABLE `grocery_items`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `grocery_orders`
--
ALTER TABLE `grocery_orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `grocery_order_items`
--
ALTER TABLE `grocery_order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `grocery_item_id` (`grocery_item_id`);

--
-- Indexes for table `seller_requests`
--
ALTER TABLE `seller_requests`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `eshopping_orders`
--
ALTER TABLE `eshopping_orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `eshopping_products`
--
ALTER TABLE `eshopping_products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `food_items`
--
ALTER TABLE `food_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `food_orders`
--
ALTER TABLE `food_orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `food_order_items`
--
ALTER TABLE `food_order_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `grocery_items`
--
ALTER TABLE `grocery_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `grocery_orders`
--
ALTER TABLE `grocery_orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `grocery_order_items`
--
ALTER TABLE `grocery_order_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `seller_requests`
--
ALTER TABLE `seller_requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `eshopping_orders`
--
ALTER TABLE `eshopping_orders`
  ADD CONSTRAINT `eshopping_orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `eshopping_orders_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `eshopping_products` (`id`);

--
-- Constraints for table `eshopping_products`
--
ALTER TABLE `eshopping_products`
  ADD CONSTRAINT `eshopping_products_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `food_orders`
--
ALTER TABLE `food_orders`
  ADD CONSTRAINT `food_orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `food_order_items`
--
ALTER TABLE `food_order_items`
  ADD CONSTRAINT `food_order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `food_orders` (`id`),
  ADD CONSTRAINT `food_order_items_ibfk_2` FOREIGN KEY (`food_item_id`) REFERENCES `food_items` (`id`);

--
-- Constraints for table `grocery_orders`
--
ALTER TABLE `grocery_orders`
  ADD CONSTRAINT `grocery_orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `grocery_order_items`
--
ALTER TABLE `grocery_order_items`
  ADD CONSTRAINT `grocery_order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `grocery_orders` (`id`),
  ADD CONSTRAINT `grocery_order_items_ibfk_2` FOREIGN KEY (`grocery_item_id`) REFERENCES `grocery_items` (`id`);

--
-- Constraints for table `seller_requests`
--
ALTER TABLE `seller_requests`
  ADD CONSTRAINT `seller_requests_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
