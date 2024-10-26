package net.ccscript.axfr4azuredns.server.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.ccscript.axfr4azuredns.DNSServerApp;
import net.ccscript.axfr4azuredns.i18n.ConfigurationText;

import ch.qos.cal10n.MessageConveyor;
import dev.harrel.jsonschema.Error;
import dev.harrel.jsonschema.Validator;
import dev.harrel.jsonschema.ValidatorFactory;
import dev.harrel.jsonschema.providers.GsonNode;

public final class DNSServerConfigurationFactory {

    private static MessageConveyor i18n = new MessageConveyor(Locale.ENGLISH);
    private static LocLogger logger = new LocLoggerFactory(i18n).getLocLogger(DNSServerApp.class);

    private DNSServerConfigurationFactory() {
    }

    public static DNSServerConfiguration createDNSServerConfigurationFromFile(String configurationFilePath)
        throws FileNotFoundException, IOException, DNSServerConfigurationException {

        InputStream configurationStream = new FileInputStream(configurationFilePath);
        String configurationJsonString = new String(configurationStream.readAllBytes(), StandardCharsets.UTF_8);
        configurationStream.close();

        return createDNSServerConfiguration(configurationJsonString);
    }

    public static DNSServerConfiguration createDNSServerConfigurationFromStream(
        InputStream configurationStream) throws FileNotFoundException, IOException,
        DNSServerConfigurationException {

        String configurationJsonString = new String(configurationStream.readAllBytes(), StandardCharsets.UTF_8);
        configurationStream.close();

        return createDNSServerConfiguration(configurationJsonString);
    }

    /**
     * Checks and loads the DNSServerConfiguration from a JSON file.
     * @param configurationJsonString The json configuration, as a String.
     * @return The deserialized {@link DNSServerConfiguration}
     * @throws FileNotFoundException If the file was not found.
     * @throws IOException If the file or the schema could not be read.
     * @throws DNSServerConfigurationException If the configuration or schema are not compliant.
     */
    public static DNSServerConfiguration createDNSServerConfiguration(String configurationJsonString)
        throws FileNotFoundException, IOException, DNSServerConfigurationException {

        logger.debug(ConfigurationText.FACTORY_CHECKJSONSCHEMA_LOG);
        checkServerConfigurationFormat(configurationJsonString);

        logger.debug(ConfigurationText.FACTORY_LOADJSON_LOG);
        DNSServerConfiguration serverConfiguration = loadServerConfiguration(configurationJsonString);

        logger.info(ConfigurationText.FACTORY_LOADJSON_OK_LOG);
        return serverConfiguration;
    }

    /**
     * Checks the server configuration is compliant with the JSON Schema.
     * @param configurationJsonString The JSON formatted configuration.
     * @throws IOException in case the file cannot be accessed.
     * @throws DNSServerConfigurationException if the configuration file isn't compliant with the schema.
     */
    private static void checkServerConfigurationFormat(String configurationJsonString)
        throws IOException, DNSServerConfigurationException {

        logger.debug(ConfigurationText.FACTORY_CHECKJSONSCHEMA_LOAD_LOG);

        InputStream schemaStream = DNSServerConfiguration.class.getResourceAsStream(
            "/net/ccscript/axfr4azuredns/server/configuration/configuration.schema.json");
        String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
        schemaStream.close();

        Validator validator = new ValidatorFactory().withJsonNodeFactory(new GsonNode.Factory()).createValidator();
        URI schemaUri = validator.registerSchema(schema);

        Validator.Result validationResult = validator.validate(schemaUri, configurationJsonString);
        if (validationResult.isValid()) {
            logger.debug(ConfigurationText.FACTORY_CHECKJSONSCHEMA_OK_LOG);
        } else {
            logger.error(ConfigurationText.FACTORY_CHECKJSONSCHEMA_ERRORS_LOG,
                validationResult.getErrors().stream().map(Error::getError).
                    collect(Collectors.joining(", "))
            );
            throw new DNSServerConfigurationException(i18n.getMessage(ConfigurationText.FACTORY_SCHEMAERROR_EXCEPTION));
        }
    }

    /**
     * Loads {@link DNSServerConfiguration} based on a file path as a string.
     * @param configurationFileName The file path as a String.
     * @return the {@link DNSServerConfiguration} based on JSON content of the file.
     * @throws FileNotFoundException If the file was not found.
     * @throws DNSServerConfigurationException If the configuration file is not in the correct format.
     */
    private static DNSServerConfiguration loadServerConfiguration(String configurationJsonString)
        throws FileNotFoundException, DNSServerConfigurationException {

        try {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(DNSServerConfiguration.class, new DNSServerConfigurationDeserializer())
                .create();

            DNSServerConfiguration configuration = gson.fromJson(
                configurationJsonString, DNSServerConfiguration.class);

            return configuration;
        } catch (JsonParseException jpe) {
            throw new DNSServerConfigurationException(i18n.getMessage(
                ConfigurationText.FACTORY_JSONERROR_EXCEPTION), jpe);
        }
    }

}
