package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.encryption.KeyStoreTextEncryptorLocator;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableConfigServer
public class Application {
    @Bean
    public KeyProperties.KeyStore keyStore() {
        Resource resource = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("encrypt.jks"));

        /**
         * NOTE!!!!! For this to work, the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8
         * must be installed in your JRE security folder.
         */
        KeyProperties.KeyStore keyStore = new KeyProperties.KeyStore();
        keyStore.setAlias("mytestkey");
        keyStore.setLocation(resource);
        keyStore.setPassword("letmein");
        keyStore.setSecret("changeme");

        return keyStore;
    }

    @Bean
    public TextEncryptorLocator textEncryptorLocator(KeyProperties.KeyStore keyStore) {
        KeyStoreTextEncryptorLocator locator = new KeyStoreTextEncryptorLocator(
                new KeyStoreKeyFactory(keyStore.getLocation(), keyStore.getPassword().toCharArray()),
                keyStore.getSecret(), keyStore.getAlias());

        return locator;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
