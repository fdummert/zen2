package de.zeos.zen2.ctrl;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

public class ActivationMediaTypeFactory {
    private static final FileTypeMap fileTypeMap;

    static {
        fileTypeMap = loadFileTypeMapFromContextSupportModule();
    }

    private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
        // see if we can find the extended mime.types from the context-support module
        Resource mappingLocation = new ClassPathResource("org/springframework/mail/javamail/mime.types");
        if (mappingLocation.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = mappingLocation.getInputStream();
                return new MimetypesFileTypeMap(inputStream);
            } catch (IOException ex) {
                // ignore
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
        return FileTypeMap.getDefaultFileTypeMap();
    }

    public static MediaType getMediaType(String file) {
        String mediaType = fileTypeMap.getContentType(file);
        return (StringUtils.hasText(mediaType) ? MediaType.parseMediaType(mediaType) : null);
    }
}
