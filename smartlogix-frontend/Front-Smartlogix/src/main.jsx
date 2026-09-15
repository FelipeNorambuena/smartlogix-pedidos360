import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { MsalProvider } from '@azure/msal-react'
import { msalInstance, msalReady } from './auth/msalInstance'
import './index.css'
import App from './App.jsx'

// MSAL debe terminar de inicializarse (recupera cuenta/tokens del cache)
// antes de renderizar cualquier componente que use useMsal()/useIsAuthenticated().
msalReady.then(() => {
  createRoot(document.getElementById('root')).render(
    <StrictMode>
      <MsalProvider instance={msalInstance}>
        <App />
      </MsalProvider>
    </StrictMode>,
  )
})
