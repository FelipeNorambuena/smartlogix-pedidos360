import { PublicClientApplication, EventType } from '@azure/msal-browser'
import { msalConfig } from './msalConfig'

export const msalInstance = new PublicClientApplication(msalConfig)

// msal-browser v3+ exige inicializar la instancia antes de usar cualquier
// otro metodo. main.jsx espera esta promesa antes de montar la app.
// Se usa loginRedirect (no loginPopup): el flujo de popup depende de que
// la ventana principal pueda leer window.location de la ventana emergente,
// lo que algunos navegadores/extensiones bloquean (BrowserAuthError:
// timed_out). El redirect evita ese problema por completo.
export const msalReady = msalInstance.initialize().then(async () => {
  await msalInstance.handleRedirectPromise().then((response) => {
    if (response?.account) {
      msalInstance.setActiveAccount(response.account)
    }
  })

  const existingAccounts = msalInstance.getAllAccounts()
  if (existingAccounts.length > 0 && !msalInstance.getActiveAccount()) {
    msalInstance.setActiveAccount(existingAccounts[0])
  }

  msalInstance.addEventCallback((event) => {
    if (
      (event.eventType === EventType.LOGIN_SUCCESS ||
        event.eventType === EventType.ACQUIRE_TOKEN_SUCCESS) &&
      event.payload?.account
    ) {
      msalInstance.setActiveAccount(event.payload.account)
    }
  })
})
