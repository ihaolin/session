package me.hao0.session.core;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import me.hao0.session.api.Serializer;
import me.hao0.session.api.SessionIdGenerator;
import me.hao0.session.api.SessionManager;
import me.hao0.session.util.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Properties;

/**
 * Abstract Session Manager
 */
public abstract class AbstractSessionManager implements SessionManager {

    private final static Logger log = LoggerFactory.getLogger(AbstractSessionManager.class);

    private static final String DEFAULT_PROPERTIES = "session.properties";

    protected SessionIdGenerator sessionIdGenerator;

    protected Serializer serializer;

    public AbstractSessionManager() throws IOException {
        this(DEFAULT_PROPERTIES);
    }

    /**
     * @param propertiesFile properties file in classpath, default is session.properties
     */
    public AbstractSessionManager(String propertiesFile) throws IOException {
        Properties props = PropertiesReader.read(propertiesFile);
        initSessionIdGenerator(props);
        initSerializer(props);
        init(props);
    }

    /**
     * init subclass
     */
    protected void init(Properties props){}

    protected void initSerializer(Properties props) {
        String sessionSerializer = (String)props.get("session.serializer");
        if (Strings.isNullOrEmpty(sessionSerializer)){
            serializer = new JsonSerializer();
        } else {
            try {
                serializer = (Serializer)(Class.forName(sessionSerializer).newInstance());
            } catch (Exception e) {
                log.error("failed to init json generator: {}", Throwables.getStackTraceAsString(e));
            } finally {
                if (sessionIdGenerator == null){
                    log.info("use default json serializer [JsonSerializer]");
                    serializer = new JsonSerializer();
                }
            }
        }
    }

    protected void initSessionIdGenerator(Properties props) {
        String sessionIdGeneratorClazz = (String)props.get("session.id.generator");
        if (Strings.isNullOrEmpty(sessionIdGeneratorClazz)){
            sessionIdGenerator = new DefaultSessionIdGenerator();
        } else {
            try {
                sessionIdGenerator = (SessionIdGenerator)(Class.forName(sessionIdGeneratorClazz).newInstance());
            } catch (Exception e) {
                log.error("failed to init session id generator: {}", Throwables.getStackTraceAsString(e));
            } finally {
                if (sessionIdGenerator == null){
                    log.info("use default session id generator[DefaultSessionIdGenerator]");
                    sessionIdGenerator = new DefaultSessionIdGenerator();
                }
            }
        }
    }
}
