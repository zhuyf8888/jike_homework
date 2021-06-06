package zhuyf.spring.springbean01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zhuyf.spring.springbean01.bean.Person;

@Configuration
public class BeanConfig {

    @Bean
    public Person createPerson() {
        return new Person();
    }
}
