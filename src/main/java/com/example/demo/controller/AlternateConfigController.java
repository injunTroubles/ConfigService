package com.example.demo.controller;

import com.example.demo.util.FileUtil;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
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

    @RequestMapping(path = "{directory}/{fileName}.json", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getJson(@PathVariable final String directory, @PathVariable final String fileName, final HttpServletRequest request) throws Exception {
        String fileToFind = FilenameUtils.getName(request.getRequestURL().toString());
        forceClone(fileToFind);
        String json = FileUtil.getFileText(directory, fileToFind);
        Map<?, ?> decryptedPropMap = decryptPropertyMap(JsonFlattener.flattenAsMap(json));

        return JsonUnflattener.unflatten(objectMapper.writeValueAsString(decryptedPropMap));
    }

    @RequestMapping(path = {"{directory}/{fileName}.yaml", "{directory}/{fileName}.yml"}, method = RequestMethod.GET)
    public String getYaml(@PathVariable final String directory, @PathVariable final String fileName, final HttpServletRequest request) throws Exception {
        String fileToFind = FilenameUtils.getName(request.getRequestURL().toString());
        forceClone(fileToFind);
        String yaml = FileUtil.getFileText(directory, fileToFind);
        Map<?, ?> decryptedPropMap = decryptPropertyMap((Map<?, ?>) new Yaml().load(yaml));

        return new Yaml().dumpAsMap(decryptedPropMap);
    }

    private Map<?, ?> decryptPropertyMap(Map<?, ?> propertyMap) {
        Environment environment = new Environment("arbitrary", "arbitrary");
        PropertySource propertySource = new PropertySource("arbitrary", propertyMap);
        environment.add(propertySource);
        environment = this.environmentEncryptor.decrypt(environment);

        return environment.getPropertySources().get(0).getSource();
    }

    private void forceClone(String fileName) {
        resourceRepository.findOne("arbitrary", "arbitrary", defaultBranch, fileName);
    }
}
