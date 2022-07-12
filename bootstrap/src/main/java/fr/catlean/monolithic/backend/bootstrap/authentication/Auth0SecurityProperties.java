package fr.catlean.monolithic.backend.bootstrap.authentication;

import lombok.Data;

@Data
public class Auth0SecurityProperties {

    String apiAudience;
    String apiIssuer;
}
