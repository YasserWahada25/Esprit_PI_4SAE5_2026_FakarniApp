export const environment = {
    production: false,
    /**
     * Chaîne vide = requêtes relatives (ng serve) ; proxy.conf.json envoie /api et /uploads → Gateway (8090).
     * À lancer pour les activités : Gateway-Service (8090) + activite-educative-service (8084) + MySQL.
     * La gateway route /api/activities vers 8084 via gateway.direct.activite-educative-service-uri (voir application.properties).
     * Sans gateway : apiBaseUrl = 'http://localhost:8084' et appeler le microservice directement (CORS déjà prévu côté 8084).
     */
    apiBaseUrl: ''
};
