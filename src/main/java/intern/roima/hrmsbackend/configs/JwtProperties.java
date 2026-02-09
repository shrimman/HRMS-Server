package intern.roima.hrmsbackend.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtProperties {
    private String secret;
    private long expiration;
    private long refreshExpiration;
}
