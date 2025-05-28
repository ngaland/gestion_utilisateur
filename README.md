# gestion_utilisateur

## Application de Gestion des Utilisateurs
Cette application est une API REST développée avec Spring Boot, permettant de créer, consulter, modifier et supprimer des utilisateurs. 
Chaque utilisateur est défini par un nom, un email, un mot de passe et un rôle (ROLE_USER, ROLE_ADMIN); La sécurité est gérée par JWT.

## Fonctionnalités
    Authentification des utilisateurs via JWT
    Créer un nouvel utilisateur
    Récupérer tous les utilisateurs (Admin seulement)
    Récupérer un utilisateur par ID (Admin ou l'utilisateur lui-même)
    Modifier un utilisateur existant (Admin seulement)
    Supprimer un utilisateur (Admin seulement)
    
## Technologies utilisées
    Java 17+
    Spring Boot
    Spring Security (JWT)
    Spring Data JPA
    PostgreSQL
    MapStruct
    maven
    Lombok
    Swagger (OpenAPI 3)   
    
##  Lancer le projet
1. Cloner le dépôt :git clone https://github.com/ton-utilisateur/gestion-utilisateur.git (Remplacez "ton-utilisateur" par ton nom d'utilisateur GitHub réel)
2. Démarrer l'application--> mvn spring-boot:run
   
## Points d'entrée de l'API (Endpoints)
1.Documentation Swagger UI: http://localhost:8080/swagger-ui.html
2.URL de base de l'API (Utilisateurs): http://localhost:8080/api/users
3.URL de base de l'API (Authentification): http://localhost:8080/auth/login

## Exemple de creation d'un utilisateur(admin):http://localhost:8080/swagger-ui/index.html#/User%20Management/createUser
{
  "name": "ngaland",
  "email": "ngaland@example.com",
  "password": "ngaland123!",       
  "roles": ["ADMIN"]
}

## Response body
{
  "id": 1,
  "name": "ngaland",
  "email": "ngalandd@gmail.com",
  "roles": ["ROLE_ADMIN"]
}

## Exemple de login(admin):http://localhost:8080/swagger-ui/index.html#/Authentication/authenticate
{
  "email": "ngaland@example.com",
  "password": "ngaland123"
}

    
