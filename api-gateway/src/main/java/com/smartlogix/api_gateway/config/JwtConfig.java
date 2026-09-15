package com.smartlogix.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    /*
     * El Gateway (BFF) valida el JWT emitido por Azure AD / Microsoft Entra ID
     * (tenant DSY1107005V, app "Pedidos360-SPA") en lugar del JWT HMAC propio
     * que emitia auth-service. Se valida: firma (via JWKS del tenant),
     * issuer y audience.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.issuer-uri}") String issuerUri,
            @Value("${security.jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator));

        return decoder;
    }
}
