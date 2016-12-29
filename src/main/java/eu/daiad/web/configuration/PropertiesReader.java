package eu.daiad.web.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Read properties from external configuration files.
 * 
 * In some cases we cannot use @{@link PropertySource} because of naming conflicts
 * on property names.
 */
@Component
class PropertiesReader
{   
    @Autowired
    private ResourceLoader resourceLoader;
    
    /**
     * Read a {@link Properties} object from a file resource
     * 
     * If the file is not found or an i/o exception is thrown, then @{code null}
     * is returned. 
     * 
     * @param path
     * @return 
     */
    public Properties read(String path)
    {
        Properties p = null;
        
        Resource r = resourceLoader.getResource(path);
        InputStream in;
        try {
            in = r.getInputStream();
        } catch (IOException e1) {
            in = null;
        }
        
        if (in != null) {
            p = new Properties(); 
            try {
                p.load(in);
            } catch (IOException e) {
                p = null;
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // Turn into an unchecked exception
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

        return p;
    }
}
