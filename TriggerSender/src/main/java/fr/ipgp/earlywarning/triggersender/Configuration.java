package fr.ipgp.earlywarning.triggersender;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Configuration {
    static String path;
    static JSONObject configuration;

    public static final List<Server> servers = new ArrayList<>();
    public static final List<Map<String, String>> triggers = new ArrayList<>();

    public static void readConfiguration(String path) throws FileNotFoundException {
        System.out.println("Reading configuration from '" + path + "'");

        Configuration.path = path;
        File f = new File(path);

        if (!f.exists())
            throw new FileNotFoundException("'" + path + "' does not exist.");


        try {
            configuration = new JSONObject(readFile(f));
        } catch (IOException e) {
            System.err.println("Cannot read configuration file.");
            System.exit(-1);
        }


        System.out.println(configuration.toString());

        readServers();
        readTriggers();
    }

    static String readFile(File f) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuilder sb = new StringBuilder();

        String s;
        while ((s = br.readLine()) != null)
            sb.append(s);

        return sb.toString();
    }

    static void readServers() {
        JSONArray serverList = configuration.getJSONArray("instances");
        for (int i = 0; i < serverList.length(); i++) {
            JSONObject obj = serverList.getJSONObject(i);

            servers.add(new Server(
                    obj.getString("host"),
                    obj.getInt("port")
            ));

            System.out.println("Added server " + servers.get(servers.size() - 1).toString());
        }
    }

    static void readTriggers() {
        JSONArray triggerList = configuration.getJSONArray("triggers");
        for (int i = 0; i < triggerList.length(); i++) {
            JSONObject obj = triggerList.getJSONObject(i);

            HashMap<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(obj.getLong("id")));
            map.put("priority", String.valueOf(obj.getInt("priority")));
            map.put("date", obj.getString("date"));
            map.put("application", obj.getString("application"));
            map.put("contactlist", obj.getString("contactlist"));
            map.put("repeat", String.valueOf(obj.getBoolean("repeat")));
            map.put("code", obj.getString("code"));
            map.put("message", obj.getString("message"));

            triggers.add(map);

            System.out.println("Added trigger " + triggers.get(triggers.size() - 1).toString());
        }
    }
}
