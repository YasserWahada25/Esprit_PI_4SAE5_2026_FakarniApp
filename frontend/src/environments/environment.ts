export const environment = {
  production: false,
  /** Prefixe pour les appels session (vide = relatif, proxy -> session-service). */
  apiUrl: '',
  wsUrl: '/ws',
  /**
   * Base pour /api/... (events, maps, engagement, activites). Vide = relatif via proxy -> Gateway.
   * Gateway route /api/activities vers activite-educative-service (voir gateway.direct.activite-educative-service-uri).
   */
  apiBaseUrl: '',
  /**
   * Google OAuth : GCP → Identifiants → client Web → « Origines JavaScript autorisées »
   * doit contenir exactement la même origine que la barre d’adresse (ex. http://localhost:4200).
   * Vide '' pour désactiver le bouton Google en attendant la config GCP.
   */
  googleClientId: '968599520946-llp69cv61a73f9457lpedn7m4tflrr2t.apps.googleusercontent.com',
  facebookAppId: '1270980888473415'
};
