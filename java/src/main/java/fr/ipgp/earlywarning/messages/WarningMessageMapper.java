package fr.ipgp.earlywarning.messages;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.gateway.Gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An utility that allows to map, for a given <code>Gateway</code> warning message names to file names to play.
 *
 * @author Thomas Kowalski
 * // TODO: add Javadoc for class
 */
public class WarningMessageMapper {
    private static Map<String, WarningMessageMapper> mappers;
    private final Map<String, String> mappings;
    private final String gatewayQualifier;

    private WarningMessageMapper(String qualifier) throws NoSuchMessageException {
        // Initialize the mappings for this gateway type
        mappings = new HashMap<>();
        gatewayQualifier = qualifier;

        // Try and fetch the default warning message from the configuration
        try {
            String defaultWarning = EarlyWarning.configuration.getString("sounds.default." + gatewayQualifier);
            mappings.put("default", defaultWarning);
        } catch (NoSuchElementException ex) {
            throw new NoSuchMessageException("Default message doesn't exist for gateway '" + gatewayQualifier + "' (should be 'sounds.default." + gatewayQualifier + "')");
        }
    }

    private static void initMappers() {
        if (mappers == null)
            mappers = new HashMap<>();
    }

    @SuppressWarnings("WeakerAccess")
    public static void testDefaultMessage(String qualifier) throws NoSuchMessageException {
        initMappers();
        mappers.put(qualifier, new WarningMessageMapper(qualifier));
    }

    public static void testDefaultMessage(Gateway gateway) throws NoSuchMessageException {
        testDefaultMessage(gateway.getSettingsQualifier());
    }

    public static WarningMessageMapper getInstance(String qualifier) {
        if (!mappers.containsKey(qualifier)) {
            EarlyWarning.appLogger.fatal("Default warning message has not been tested for '" + qualifier + "'. Please verify that you do it with WarningMessageMapper.testDefaultMessage(gateway).");
            System.exit(-1);
        }

        return mappers.get(qualifier);
    }

    public static WarningMessageMapper getInstance(Gateway gateway) {
        return getInstance(gateway.getSettingsQualifier());
    }

    @SuppressWarnings("WeakerAccess")
    public String getName(String id) throws NoSuchMessageException {
        if (mappings.containsKey(id))
            return mappings.get(id);

        try {
            String name = EarlyWarning.configuration.getString("sounds." + id + "." + gatewayQualifier);
            mappings.put(id, name);
            return name;
        } catch (NoSuchElementException ex) {
            throw new NoSuchMessageException(id);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public String getDefault() {
        return mappings.get("default");
    }

    public String getNameOrDefault(String id) {
        try {
            return getName(id);
        } catch (NoSuchMessageException e) {
            return getDefault();
        }
    }
}
