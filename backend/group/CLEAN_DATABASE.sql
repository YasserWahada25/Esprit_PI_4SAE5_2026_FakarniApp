-- Script pour nettoyer la base de données du Group-Service
-- À exécuter si vous avez des données de test avec des IDs numériques

USE group_service_db;

-- Sauvegarder les données (optionnel)
-- CREATE TABLE groups_backup AS SELECT * FROM groups;
-- CREATE TABLE group_members_backup AS SELECT * FROM group_members;

-- Supprimer toutes les données
DELETE FROM group_members;
DELETE FROM groups;

-- Réinitialiser les auto-increment
ALTER TABLE groups AUTO_INCREMENT = 1;
ALTER TABLE group_members AUTO_INCREMENT = 1;

-- Vérifier que les tables sont vides
SELECT COUNT(*) as groups_count FROM groups;
SELECT COUNT(*) as members_count FROM group_members;

-- Vérifier la structure des colonnes
DESCRIBE groups;
DESCRIBE group_members;
