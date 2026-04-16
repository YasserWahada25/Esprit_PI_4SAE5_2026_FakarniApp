-- Script pour migrer les données existantes vers des IDs MongoDB
-- ATTENTION: Vous devez d'abord créer les utilisateurs correspondants dans le User-Service

USE group_service_db;

-- 1. Voir les données actuelles
SELECT id, name, creator_id FROM groups;
SELECT id, group_id, user_id, role FROM group_members;

-- 2. Exemple de migration manuelle
-- Remplacez 'VOTRE_USER_ID_MONGODB' par un vrai ID MongoDB du User-Service
-- Exemple: '675e1234567890abcdef1234'

-- Mettre à jour le créateur du groupe 1
-- UPDATE groups 
-- SET creator_id = '675e1234567890abcdef1234' 
-- WHERE id = 1;

-- Mettre à jour les membres du groupe 1
-- UPDATE group_members 
-- SET user_id = '675e1234567890abcdef1234' 
-- WHERE group_id = 1 AND user_id = '1';

-- 3. Vérifier les modifications
SELECT id, name, creator_id FROM groups;
SELECT id, group_id, user_id, role FROM group_members;

-- NOTES:
-- - Vous devez avoir des utilisateurs existants dans le User-Service
-- - Récupérez leurs IDs MongoDB depuis la collection 'users'
-- - Remplacez les anciens IDs numériques par les nouveaux IDs MongoDB
