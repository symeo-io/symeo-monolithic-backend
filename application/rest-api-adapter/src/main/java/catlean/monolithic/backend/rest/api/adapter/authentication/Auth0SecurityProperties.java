package catlean.monolithic.backend.rest.api.adapter.authentication;

import lombok.Data;

@Data
public class Auth0SecurityProperties {

    String apiAudience;
    String apiIssuer;
}
