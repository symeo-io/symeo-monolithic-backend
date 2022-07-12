package fr.catlean.monolithic.backend.bootstrap.cors;

import lombok.Data;

@Data
public class WebCorsProperties {
    private String[] hosts;
}
