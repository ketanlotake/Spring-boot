package jp.co.apidemo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    

    public static final String AUT_HEADER ="Authorization";

    private ApiKey apiKeys()
    {
       return new ApiKey("Token Access",AUT_HEADER,"header");
    }

    private List<SecurityContext> securityContexts()
    {
        return Arrays.asList(SecurityContext.builder().securityReferences(securityReferences()).build());
    }

    private List<SecurityReference> securityReferences()
    {
        AuthorizationScope[] authorizationScope= {new AuthorizationScope("Unlimited","Full API Permission")};
        return Collections.singletonList(new SecurityReference("Token Access",authorizationScope ));
    }
    
    @Bean
    public Docket api()
    {
        return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(getInfo())
        .securityContexts(securityContexts())
        .securitySchemes(Arrays.asList(apiKeys()))
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build();
    }

    private ApiInfo getInfo()
    {
        return new ApiInfo("Employee Application","Description","1.0","Terms of service",new Contact("Test","EmployeeManagement.com","Test@gmail.com"),"License of APIS","License URL",Collections.emptyList());
    }

}
