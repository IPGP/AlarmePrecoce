package fr.ipgp.earlywarning.messages;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.gateway.Gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.ipgp.earlywarning.utilities.ConfigurationValidator.getItems;

/**
 * An utility that allows to map, for a given {@link Gateway}, the warning message ids to the names of files to play.
 *
 * @author Thomas Kowalski
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

    /**
     * Verify that, for a given <code>qualifier</code>, the default warning message is configured.
     *
     * @param qualifier the {@link Gateway}'s qualifier
     * @throws NoSuchMessageException if the default message is not configured
     */
    @SuppressWarnings("WeakerAccess")
    public static void testDefaultMessage(String qualifier) throws NoSuchMessageException {
        initMappers();
        mappers.put(qualifier, new WarningMessageMapper(qualifier));
    }

    /**
     * Verify that the default warning message is configured, for a given {@link Gateway}.
     *
     * @param gateway the {@link Gateway} to check
     * @throws NoSuchMessageException if the default message is not configured
     */
    public static void testDefaultMessage(Gateway gateway) throws NoSuchMessageException {
        testDefaultMessage(gateway.getSettingsQualifier());
    }

    /**
     * Returns the unique mapper for a given qualifier.
     *
     * @param qualifier the qualifier to use
     * @return the {@link WarningMessageMapper} instance corresponding to this qualifier
     */
    public static WarningMessageMapper getInstance(String qualifier) {
        initMappers();

        if (!mappers.containsKey(qualifier)) {
            EarlyWarning.appLogger.fatal("Default warning message has not been tested for '" + qualifier + "'. Please verify that you do it with WarningMessageMapper.testDefaultMessage(gateway).");
            System.exit(-1);
        }

        return mappers.get(qualifier);
    }

    /**
     * Returns the unique mapper for a given {@link Gateway}
     *
     * @param gateway the {@link Gateway} to use
     * @return the {@link WarningMessageMapper} instance corresponding to this {@link Gateway}
     */
    public static WarningMessageMapper getInstance(Gateway gateway) {
        return getInstance(gateway.getSettingsQualifier());
    }

    /**
     * Returns the name of the file to play on this gateway, for a given ID
     *
     * @param id the ID of the sound
     * @return the name of the corresponding file
     * @throws NoSuchMessageException if the sound is not configured. See <code>getNameOrDefault</code>
     */
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

    /**
     * Returns the default warning message for this gateway
     *
     * @return the name of the default warning message file
     */
    @SuppressWarnings("WeakerAccess")
    public String getDefault() {
        return mappings.get("default");
    }

    /**
     * Tries to find a mapping for a given ID, returns the default message if it is not configured
     *
     * @param id the ID to find a mapping for
     * @return the corresponding file name, or if it is not configured, the default file name
     */
    public String getNameOrDefault(String id) {
        try {
            return getName(id);
        } catch (NoSuchMessageException ex) {
            EarlyWarning.appLogger.warn("Mapping for '" + id + "' not found, defaulting to '" + getDefault() + "'");
            return getDefault();
        }
    }

    public List<String> getAvailableMessages() {
        List<String> sounds = new ArrayList<>();
        List<Map<String, String>> messageEntries = getItems("sounds.sound");
        for (Map<String, String> messageEntry : messageEntries)
            if (messageEntry.containsKey(gatewayQualifier))
                sounds.add(messageEntry.get("id"));

        return sounds;
    }

    public String getNameIgnoreCase(String id) throws NoSuchMessageException {
        for (String k : getAvailableMessages())
            if (k.equalsIgnoreCase(id)) {
                if (!k.equals(id))
                    EarlyWarning.appLogger.info("Mapping '" + id + "' to '" + k + "' (ignoring case)");
                return getName(k);
            }

        throw new NoSuchMessageException(id);
    }

    public String getNameOrDefaultIgnoreCase(String id) {
        try {
            return getNameIgnoreCase(id);
        } catch (NoSuchMessageException ex) {
            EarlyWarning.appLogger.warn("Mapping for '" + id + "' (ignore case) not found, defaulting to '" + getDefault() + "'");
            return getDefault();
        }
    }
}
