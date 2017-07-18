package com.example.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.server.resource.NoSuchResourceException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by rvann on 7/17/17.
 */
@Component
public class FileUtil {

    @Value("${spring.cloud.config.server.git.basedir:#{systemProperties['java.io.tmpdir']}}")
    private String baseDir;

    public String getFileText(String directory, String fileName) {
        String filePath = getFilePath(directory, fileName);
        StringWriter writer = new StringWriter();
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.forEach(line -> writer.append(line + "\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    private String getFilePath(String directory, String fileName) {
        String str = null;
        try {
            str = Files.find(Paths.get(baseDir), 5,
                    (path, attr) -> path.getFileName().endsWith(fileName))
                    .filter(path -> path.getParent().endsWith(directory))
                    .findFirst().map(foundPath -> foundPath.toString())
                    .orElseThrow(() -> new NoSuchResourceException("Not found: " + directory + "/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }
}
