package com.example.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.encryption.EnvironmentEncryptor;
import org.springframework.cloud.config.server.resource.ResourceRepository;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by rvann on 7/14/17.
 */
@RestController
@RequestMapping("alternate")
public class AlternateConfigController {

    private EnvironmentEncryptor environmentEncryptor;
    private ResourceRepository resourceRepository;
    @Value("${spring.cloud.config.server.defaultBranch:master}")
    private String defaultBranch;
    private ObjectMapper objectMapper;

    @Autowired
    AlternateConfigController(EnvironmentEncryptor environmentEncryptor, ResourceRepository resourceRepository) {
        this.environmentEncryptor = environmentEncryptor;
        this.resourceRepository = resourceRepository;
        this.objectMapper = new ObjectMapper();
    }

    @RequestMapping(path = "{application}/{fileName}.json", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getJson(@PathVariable final String application, @PathVariable final String fileName) throws Exception {
        InputStream is = resourceRepository.findOne(application, "arbitrary", defaultBranch, fileName + ".json").getInputStream();
        String json = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
        Environment environment = getEnvironment(JsonFlattener.flattenAsMap(json));
        String responseJson = JsonUnflattener.unflatten(objectMapper.writeValueAsString(environment.getPropertySources().get(0).getSource()));

        return responseJson;
    }

    @RequestMapping(path = {"{application}/{fileName}.yaml", "{application}/{fileName}.yml"}, method = RequestMethod.GET)
    public String getYaml(@PathVariable final String application, @PathVariable final String fileName, final HttpServletRequest request) throws Exception {
        String fileToFind = FilenameUtils.getName(request.getRequestURL().toString());
        InputStream is = resourceRepository.findOne(application, "arbitrary", defaultBranch, fileToFind).getInputStream();
        Environment environment = getEnvironment((Map<?, ?>) new Yaml().load(is));

        return new Yaml().dumpAsMap(environment.getPropertySources().get(0).getSource());
    }

    private Environment getEnvironment(Map<?, ?> propertyMap) {
        Environment environment = new Environment("blah", "blah");
        PropertySource propertySource = new PropertySource("blah", propertyMap);
        environment.add(propertySource);
        environment = this.environmentEncryptor.decrypt(environment);

        return environment;
    }
}
