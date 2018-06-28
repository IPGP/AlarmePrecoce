-- phpMyAdmin SQL Dump
-- version 2.9.1.1-Debian-6
-- http://www.phpmyadmin.net
-- 
-- Serveur: localhost
-- Généré le : Lundi 10 Mars 2008 à 15:12
-- Version du serveur: 5.0.32
-- Version de PHP: 5.2.0-8+etch10
-- 
-- Base de données: `fonctionnement`
-- 

-- --------------------------------------------------------

-- 
-- Structure de la table `acquisition`
-- 

CREATE TABLE `acquisition` (
  `num_appli` int(11) NOT NULL,
  `num_type` int(11) NOT NULL,
  `date_heure` datetime NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- 
-- Contenu de la table `acquisition`
-- 

INSERT INTO `acquisition` (`num_appli`, `num_type`, `date_heure`) VALUES 
(20, 24, '2008-03-10 14:12:32'),
(20, 18, '2008-03-10 14:11:13');
