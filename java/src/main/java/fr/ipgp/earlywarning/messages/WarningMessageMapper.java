package fr.ipgp.earlywarning.messages;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.gateway.Gateway;

import java.util.HashMap;
import java.util.Map;

import static fr.ipgp.earlywarning.utilities.ConfigurationValidator.getItems;

/**
 * An utility that allows to map, for a given {@link Gateway} warning message names to file names to play.
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
        String defaultWarning = null;

        for (Map<String, String> soundEntry : getItems("sounds.sound"))
            if (soundEntry.get("id").equalsIgnoreCase("default"))
                defaultWarning = soundEntry.get(qualifier);

        if (defaultWarning == null)
            throw new NoSuchMessageException("Default message doesn't exist for gateway '" + gatewayQualifier + "' in configuration.");

        mappings.put("default", defaultWarning);
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
        initMappers();

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
        if (id == null)
            throw new NoSuchMessageException("(null)");

        if (mappings.containsKey(id))
            return mappings.get(id);

        String name = null;
        for (Map<String, String> soundEntry : getItems("sounds.sound")) {
            if (soundEntry.get("id").equals(id))
                name = soundEntry.get(gatewayQualifier);
        }

        if (name == null)
            throw new NoSuchMessageException(id);

        mappings.put(id, name);
        return name;
    }

    @SuppressWarnings("WeakerAccess")
    public String getDefault() {
        return mappings.get("default");
    }

    public String getNameOrDefault(String id) {
        try {
            return getName(id);
        } catch (NoSuchMessageException ex) {
            return getDefault();
        }
    }
}
