# ConfigService

## Overview

> Spring Cloud Config provides server and client-side support for externalized configuration in a distributed system. With the Config Server you have a central place to manage external properties for applications across all environments. The concepts on both client and server map identically to the Spring Environment and PropertySource abstractions, so they fit very well with Spring applications, but can be used with any application running in any language. As an application moves through the deployment pipeline from dev to test and into production you can manage the configuration between those environments and be certain that applications have everything they need to run when they migrate. The default implementation of the server storage backend uses git so it easily supports labelled versions of configuration environments, as well as being accessible to a wide range of tooling for managing the content. It is easy to add alternative implementations and plug them in with Spring configuration.

Further reading: https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html

This implementation provides native 'Cloud Config' behavior, but also provides alternate
json and yaml configuration options by extending native behavior.  This allows non-standard '.properties'
based files to be used by external clients, while still providing at rest encryption.  Currently,
only json and yaml are supported.

## Implementation Detail

* Spring Boot 1.5.4.RELEASE
* Spring Cloud Config Server 1.3.1.RELEASE
* Java 8 (1.8.0_91)
* Gradle 3.1
* Embedded Tomcat 8.5.15

### Security

#### Users
```java
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin").password("password").authorities("ADMIN", "ACTUATOR");
        auth.inMemoryAuthentication()
                .withUser("client").password("password").authorities("CONFIG_CLIENT");
    }
```

#### Permissions
```java
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling()
                .and()
                .authorizeRequests()
                .antMatchers("/version/**").permitAll()
                .antMatchers("/actuator/health/**").permitAll()
                .antMatchers("/actuator/info/**").permitAll()
                .antMatchers("/actuator/**", "/decrypt/**").hasAuthority("ADMIN")
                .antMatchers("/**").hasAnyAuthority("CONFIG_CLIENT", "ADMIN")
                .and().httpBasic();
    }
```

### Actuator Endpoints

* Root resource path is 'actuator'

> Actuator endpoints allow you to monitor and interact with your application. Spring Boot includes a number of built-in endpoints and you can also add your own. 

Further Reading: https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html


### OOB Config Endpoints
```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```
> where the "application" is injected as the spring.config.name in the SpringApplication (i.e. what is normally "application" in a regular Spring Boot app), "profile" is an active profile (or comma-separated list of properties), and "label" is an optional git label (defaults to "master".)

### Encryption / Decryption
https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html#_encryption_and_decryption

### Custom Config Endpoints
```
/alternate/{directory}/{fileName}.json
/alternate/{directory}/fileName}.yaml
/alternate/{directory}/fileName}.yml
```

### Setup

Add necessary detail to 'application.properties'

```
spring.cloud.config.server.git.searchPaths=*
spring.cloud.config.server.git.uri=https://github.com/[USE YOUR OWN GITHUB REPO]
spring.cloud.config.server.git.username=(USE YOUR OWN GITHUB USERNAME OR ACCESS TOKEN)
spring.cloud.config.server.git.password=(IF USING USERNAME, PROVIDE PASSWORD. LEAVE BLANK IF USING ACCESS TOKEN, BUT THE PROPERTY MUST REMAIN)
spring.cloud.config.server.defaultLabel=(EQUATES TO GITHUB BRANCH, DEFAULTS TO "master")
```

### Run Application

#### From application directory in terminal

* Using embedded application.properties
    ```
    ./gradlew clean build && java -jar build/libs/ConfigService-0.0.1-SNAPSHOT.jar
    ```
* Using provided properties
    ```
    ./gradlew clean build && java -jar -Dspring.cloud.config.server.git.username=[YOUR USERNAME] -Dspring.cloud.config.server.git.uri=[YOUR GIT REPO] build/libs/ConfigService-0.0.1-SNAPSHOT.jar
    ```

#### Verify application is running

* Request
    ```
    $ curl -X "GET" "http://localhost:8888/actuator/health"
    ```

* Response
    ```json
    {"status":"UP"}
    ```
    