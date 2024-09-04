# JAVA Logging Filter
[![JAVA 21](https://img.shields.io/badge/JAVA%20-21-blue.svg)](https://www.java.com/es/)
[![GRADLE](https://img.shields.io/badge/Gradle%20-8.8-red.svg)](https://gradle.org/)

Use this library in order to add api request/response logging capability to your microservice

### Configuration

Add the filter to your spring configuration 
```java
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter(){
        final FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/transactions/*");
        registrationBean.setOrder(2);

        return registrationBean;
    }

}
```
