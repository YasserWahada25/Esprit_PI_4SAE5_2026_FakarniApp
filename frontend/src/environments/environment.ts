export const environment = {
  production: false,
  /** Prefixe pour les appels session (vide = relatif, proxy -> session-service). */
  apiUrl: '',
  wsUrl: '/ws',
  /**
   * Base pour /api/... (events, maps, engagement, activites). Vide = relatif via proxy -> Gateway.
   * Gateway route /api/activities vers activite-educative-service (voir gateway.direct.activite-educative-service-uri).
   */
  apiBaseUrl: ''
};
