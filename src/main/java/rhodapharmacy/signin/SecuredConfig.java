package rhodapharmacy.signin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;

@Service
public class SecuredConfig {

    private static Logger log = LoggerFactory.getLogger(SecuredConfig.class);

    private final Properties properties;

    public SecuredConfig()
    throws IOException {
        File securePropertiesFile = new File(".secure.properties");
        properties = new Properties();
        if(securePropertiesFile.exists()) {
            properties.load(new FileInputStream(securePropertiesFile));
            log.debug("read {} secure properties", properties.size());
        }
        else {
            log.debug("\".secure.properties\" not found no secure properties read");
        }
    }

    public String getProperty(String name)
    throws NoSuchElementException {
        if(!properties.containsKey(name)) throw new NoSuchElementException("a property with the name \"" + name + "\" could not be found");
        return properties.getProperty(name);
    }
}
