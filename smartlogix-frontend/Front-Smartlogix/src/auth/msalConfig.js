/*
 * Configuracion de MSAL para el tenant DSY1107005V (Pedidos360).
 * clientId/authority se leen de variables de entorno para no fijar
 * los IDs del tenant academico directamente en el codigo fuente.
 */
const tenantId = import.meta.env.VITE_AZURE_TENANT_ID
const clientId = import.meta.env.VITE_AZURE_CLIENT_ID

export const msalConfig = {
  auth: {
    clientId,
    authority: `https://login.microsoftonline.com/${tenantId}`,
    redirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI || 'http://localhost:5173',
    postLogoutRedirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI || 'http://localhost:5173',
  },
  cache: {
    // sessionStorage evita que el token persista entre pestanas distintas.
    cacheLocation: 'sessionStorage',
    storeAuthStateInCookie: false,
  },
}

// Scope expuesto por la app "Pedidos360-SPA" (ver Fase 1 del informe).
export const loginRequest = {
  scopes: [`api://${clientId}/access_as_user`],
}
