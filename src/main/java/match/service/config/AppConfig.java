package match.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import match.service.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    @Value("${app.services.resume-service-url}")
    private String resumeServiceUrl;
    private final CustomUserDetailsService customUserDetailsService;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(customUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}

   @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .baseUrl(resumeServiceUrl)
                .build();
   }
   @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder){
        return  builder.modules(new JavaTimeModule()).build();
   }


    }
