/*
 * Reemplaza al formulario de email/password original: el login ahora
 * lo resuelve el IDaaS (Azure AD, tenant DSY1107005V) via MSAL.
 * Los estilos (.primary-button) vienen de auth-page.css, ya importado
 * por LoginPage.
 */
function MsalLoginButton({ onLogin }) {
  return (
    <div className="login-form">
      <p>Inicia sesi&oacute;n con tu cuenta institucional para continuar.</p>

      <button className="primary-button" type="button" onClick={onLogin}>
        Iniciar sesi&oacute;n con Microsoft
      </button>
    </div>
  )
}

export default MsalLoginButton
