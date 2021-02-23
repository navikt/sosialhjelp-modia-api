package no.nav.sbl.sosialhjelpmodiaapi.mock

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.oas.annotations.EnableOpenApi
import springfox.documentation.spi.DocumentationType.OAS_30
import springfox.documentation.spring.web.plugins.Docket

@Profile("(dev-fss | mock | mock-alt | local)")
@Configuration
@EnableOpenApi
class SwaggerConfig {

    @Bean
    fun api(): Docket {
        return Docket(OAS_30)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.regex(".*/api/.*"))
            .build()
    }
}
