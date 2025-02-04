-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : mar. 04 fév. 2025 à 18:09
-- Version du serveur : 8.2.0
-- Version de PHP : 8.2.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `evan_lucas_db_java`
--

-- --------------------------------------------------------

--
-- Structure de la table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
CREATE TABLE IF NOT EXISTS `inventory` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_id` int NOT NULL,
  `store_id` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_item_id` (`item_id`),
  KEY `fk_store_id` (`store_id`)
) ENGINE=MyISAM AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `inventory`
--

INSERT INTO `inventory` (`id`, `item_id`, `store_id`, `price`, `quantity`) VALUES
(6, 4, 1, 45.00, 35),
(7, 7, 1, 0.10, 1);

-- --------------------------------------------------------

--
-- Structure de la table `items`
--

DROP TABLE IF EXISTS `items`;
CREATE TABLE IF NOT EXISTS `items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `items`
--

INSERT INTO `items` (`id`, `name`) VALUES
(1, 'Pomme'),
(3, 'tuc'),
(4, 'bonbon'),
(7, 'andrea');

-- --------------------------------------------------------

--
-- Structure de la table `store`
--

DROP TABLE IF EXISTS `store`;
CREATE TABLE IF NOT EXISTS `store` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name_store` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `store`
--

INSERT INTO `store` (`id`, `name_store`) VALUES
(1, 'Amazon');

-- --------------------------------------------------------

--
-- Structure de la table `store_employees`
--

DROP TABLE IF EXISTS `store_employees`;
CREATE TABLE IF NOT EXISTS `store_employees` (
  `id` int NOT NULL AUTO_INCREMENT,
  `store_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_id` (`user_id`),
  KEY `fk_store_id` (`store_id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `store_employees`
--

INSERT INTO `store_employees` (`id`, `store_id`, `user_id`) VALUES
(4, 1, 4),
(7, 1, 7);

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `pseudo` varchar(50) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rôle` enum('employé','administrateur') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'employé',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `pseudo`, `email`, `password`, `rôle`) VALUES
(1, 'admin', 'administrateur@istore.fr', '$2a$10$rfOo/CIDqipBUy8kDqdVE.v5jDhKCgjrwuvgbuxaAIEHWiRXvwDOy', 'administrateur'),
(4, 'juju', 'justine.dupin@amazon.fr', '$2a$10$xP0GBisI9.LoiKZp5M4u6Ogrnh1UNfXE9syxw7pPG8z7I0u.7bluK', 'employé'),
(6, 'LucasOui', 'lucasdb@fnac.com', '$2a$10$5oci3fo0cWw.gFemHLWVHOCFGLy3kdnK1p.Yz8TQ3V1GMjErwd.Gm', 'employé'),
(8, 'test', 'test@test.test', '$2a$10$X.vJOextGt9u0mr4jbUxueINdAl1d9vEZi8y8MdhliNdFHTKs4ukK', 'employé');

-- --------------------------------------------------------

--
-- Structure de la table `white_list`
--

DROP TABLE IF EXISTS `white_list`;
CREATE TABLE IF NOT EXISTS `white_list` (
  `email` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `white_list`
--

INSERT INTO `white_list` (`email`) VALUES
('administrateur@istore.fr'),
('justine.dupin@amazon.fr'),
('employe@fnac.fr'),
('lucasdb@amazon.fr'),
('test@test.test');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
