package fr.ipgp.earlywarning.contacts;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fr.ipgp.earlywarning.EarlyWarning;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.ipgp.earlywarning.utilities.PathUtilities.buildPath;

/**
 * A basic Web server used to maintain and update the call priority list for the Early Warning Alarm.<br />
 * It uses a {@link HttpServer}
 *
 * @author Thomas Kowalski
 */
public class ContactListManagerServer {
    /**
     * The default port for the Web server
     */
    private static final int DEFAULT_PORT = 6001;

    /**
     * The port used by the server
     */
    private final int port;
    /**
     * The WWW home for the Web server's files
     */
    private final String home;

    @SuppressWarnings("WeakerAccess")
    public ContactListManagerServer(int port, String home) {
        this.port = port;
        this.home = home;
    }

    /**
     * Constructs the server with the default port (DEFAULT_PORT) {@link ContactListManagerServer}
     *
     * @param home the WWW home for the Web server
     */
    public ContactListManagerServer(String home) {
        this(DEFAULT_PORT, home);
    }

    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public int getPort() {
        return port;
    }

    /**
     * Starts the server.
     */
    public void startServer() throws IOException {
        String currentName = Thread.currentThread().getName();
        Thread.currentThread().setName("ContactServer");
        EarlyWarning.appLogger.debug("Starting contact list Web server on port " + String.valueOf(port));
        Thread.currentThread().setName(currentName);

        // Create a server and bind it to localhost:port
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        // The handler for files
        httpServer.createContext("/", new GeneralHandler(home));
        // The handler for update requests
        httpServer.createContext("/update", new PostHandler());
        // The handler for new contact requests
        httpServer.createContext("/new", new PostHandler());

        // Start the server
        httpServer.start();
    }

    /**
     * The {@link HttpHandler} used for GET requests to send files (html, js, etc.)
     *
     * @author Thomas Kowalski
     */
    static class GeneralHandler implements HttpHandler {
        /**
         * The WWW home directory on the server.
         */
        final String home;

        /**
         * Constructor
         *
         * @param home the WWW home of the handler
         */
        GeneralHandler(String home) {
            Thread.currentThread().setName("ContactServer");
            this.home = home;
        }

        /**
         * Streams a local file to an OutputStream
         *
         * @param file the file to be streamed
         * @param out  the output stream
         */
        private static void sendFile(InputStream file, OutputStream out) {
            try {
                byte[] buffer = new byte[1000];
                while (file.available() > 0)
                    out.write(buffer, 0, file.read(buffer));
            } catch (IOException ex) {
                EarlyWarning.appLogger.error(ex.getMessage());
            }
        }

        /**
         * Generates the main page from the empty index.html and serves it
         *
         * @param file         the index.html empty template
         * @param responseBody the OutputStream to write to
         * @throws IOException if index.html can't be read
         */
        private void serveIndex(ContactList list, File file, OutputStream responseBody) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
                sb.append(line).append("\n");

            String content = sb.toString();

            // Generate the HTML document from the template index.html
            Document doc = Jsoup.parse(content);
            // Create a new script element
            Element script = doc.createElement("script");
            // Find the <head> element
            Element head = doc.getElementsByTag("head").first();
            // Manually set the <script> element content
            script.html("var people = " + list.getAvailableContactsAsJson() + ";");
            script.html(script.html() + "var people_enabled = " + list.getEnabledContactsAsJson() + ";");
            // Add the <script> to the <head>
            head.appendChild(script);

            Element listsList = doc.getElementById("available-lists");
            for (String s : ContactListMapper.getInstance().getAvailableLists()) {
                Element a = doc.createElement("a");
                a.attr("href", "/index.html?list=" + s);
                a.text(s);
                listsList.appendChild(a);
            }

            // Write the completed template to the OutputStream
            responseBody.write(doc.toString().getBytes("UTF-8"));
        }

        /**
         * Verifies that the request file exists and serves it if it does.
         * If the file doesn't exist, send a 404-response.
         *
         * @param t the HttpExchange to be handled
         * @throws IOException if the file exists but can't be read.
         */
        public void handle(HttpExchange t) throws IOException {
            URI uri = t.getRequestURI();
            uri = uri.normalize();

            // Determine requested file
            String path = home + uri;
            // Remove query string
            path = path.split("\\?")[0].trim();
            // If no index file has been given, add it
            if (path.endsWith("/"))
                path = path + "index.html";

            while (path.contains("//"))
                path = path.replace("//", "/");

            // If we are serving the index, try to determine the right ContactList
            ContactList list = null;
            if (path.endsWith("index.html")) {
                try {
                    list = getListForURI(uri);
                } catch (Exception ex) {
                    EarlyWarning.appLogger.error("Exception: " + ex.getMessage());
                }
                if (list == null) {
                    serveEmpty(t, "No such list.");
                    return;
                }
            }

            File f = new File(buildPath(path));
            if (f.isFile()) {
                t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

                if (path.endsWith("index.html")) {
                    serveIndex(list, f, t.getResponseBody());
                } else
                    sendFile(new FileInputStream(f), t.getResponseBody());

                t.getResponseBody().close();
            } else {
                EarlyWarning.appLogger.warn("File not found: '" + home + uri + "'");

                serveEmpty(t, "No such file: '" + f.getName() + "'");
            }
        }

        private ContactList getListForURI(URI uri) {
            if (!uri.toString().contains("?"))
                return ContactListMapper.getInstance().getDefaultList();
            else if (uri.toString().split("\\?")[1].trim().equals(""))
                return ContactListMapper.getInstance().getDefaultList();

            Map<String, String> parameters = getQueryMap(uri.getQuery());

            if (parameters.containsKey("list")) {
                String listName = parameters.get("list");
                EarlyWarning.appLogger.info("Web server received request for list '" + listName + "'");

                try {
                    return ContactListMapper.getInstance().getList(listName);
                } catch (NoSuchListException ex) {
                    EarlyWarning.appLogger.error("User requested to modify Contact List '" + listName + "', which does not exist.");
                    return null;
                } catch (ContactListBuilder.UnimplementedContactListTypeException ignored) {
                    EarlyWarning.appLogger.error("User requested to modify Contact List '" + listName + "', which has an incorrect format.");
                    return null;
                }
            } else {
                return ContactListMapper.getInstance().getDefaultList();
            }
        }

        private void serveEmpty(HttpExchange t, String result) {
            EarlyWarning.appLogger.info("Sending empty response: '" + result + "'");

            try {
                t.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, result.length());

                OutputStream os = t.getResponseBody();
                os.write(result.getBytes());
                os.close();
            } catch (IOException ex) {
                EarlyWarning.appLogger.error("Can't write 404 response to client: " + ex.getMessage());
            }
        }
    }

    /**
     * The {@link HttpHandler} used for POST requests, which are order and enable / disable update requests.
     */
    static class PostHandler implements HttpHandler {
        private static void handleNewContact(JSONObject data) {
            String phone = data.getString("phone");
            String name = data.getString("name");
            boolean priority = data.getBoolean("priority");
            String listName = data.getString("list");

            ContactList list;
            try {
                list = ContactListMapper.getInstance().getList(listName);
            } catch (NoSuchListException ex) {
                EarlyWarning.appLogger.error("Used tried to add contact to list '" + listName + "', which does not exist.");
                return;
            } catch (ContactListBuilder.UnimplementedContactListTypeException ex) {
                EarlyWarning.appLogger.error("Used tried to add contact to list '" + listName + "', which has an invalid format.");
                return;
            }

            EarlyWarning.appLogger.debug("Adding contact " + name + " / " + phone);
            list.addContact(new Contact(name, phone, priority), true);

            try {
                list.write();
            } catch (IOException ex) {
                EarlyWarning.appLogger.error("Can't write JSONContactList: " + ex.getMessage());
            }
        }

        /**
         * Reads the data for an list-order update and updates the JSONContactList to reflect it.
         *
         * @param data the JSON object sent by the page
         */
        private static void handleUpdate(JSONObject data) {
            EarlyWarning.appLogger.info("Updating orders.");
            JSONArray enabledNames = data.getJSONArray("enabled");
            JSONArray allNames = data.getJSONArray("available");
            String listName = data.getString("list");
            ContactList list;

            try {
                list = ContactListMapper.getInstance().getList(listName);
            } catch (NoSuchListException ex) {
                EarlyWarning.appLogger.error("User tried to update contact list '" + listName + "', which does not exist.");
                return;
            } catch (ContactListBuilder.UnimplementedContactListTypeException ex) {
                EarlyWarning.appLogger.error("User tried to update contact list '" + listName + "', which has an invalid format.");
                return;
            }

            List<String> names = new ArrayList<>();
            for (int i = 0; i < enabledNames.length(); i++) {
                names.add(enabledNames.getString(i));
            }
            list.updateCallList(names);

            names.clear();
            for (int i = 0; i < allNames.length(); i++)
                names.add(allNames.getString(i));
            list.clean(names);

            try {
                list.write();
            } catch (IOException ex) {
                EarlyWarning.appLogger.error("Can't write contact list.");
            }
        }

        /**
         * Handles a <code>POST</code> request.
         *
         * @param he the exchange to handle
         * @throws IOException if the index.html template can't be read
         */
        public void handle(HttpExchange he) throws IOException {
            Thread.currentThread().setName("ContactServer");
            if (he.getRequestMethod().equalsIgnoreCase("POST")) {
                try {
                    Headers requestHeaders = he.getRequestHeaders();
                    // Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();

                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
                    String uri = he.getRequestURI().toString();

                    InputStream is = he.getRequestBody();
                    byte[] data = new byte[contentLength];
                    if (is.read(data) <= 0) {
                        EarlyWarning.appLogger.error("No data received.");
                        return;
                    }

                    // The data we need is here
                    EarlyWarning.appLogger.info("Received JSON data: " + new String(data));
                    JSONObject json = new JSONObject(new String(data));

                    uri = uri.replaceAll("/", "");
                    if (uri.equalsIgnoreCase("new"))
                        handleNewContact(json);
                    else if (uri.equalsIgnoreCase("update"))
                        handleUpdate(json);
                    else {
                        byte[] response = "{\"status\": \"error\"}".getBytes();
                        he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length);
                        OutputStream os = he.getResponseBody();
                        os.write(response);
                        he.close();
                        return;
                    }

                    // Send a confirmation that everything went well.
                    byte[] response = "{\"status\": \"success\"}".getBytes();
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    OutputStream os = he.getResponseBody();
                    os.write(response);
                    he.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                // If we get a request that is not POST, send a BAD_METHOD status with a JSON content.
                EarlyWarning.appLogger.warn("Wrong request on " + he.getRequestURI().toString() + " and method " + he.getRequestMethod());
                byte[] response = "{\"status\": \"Bad method\"}".getBytes();
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length);
                he.getResponseBody().write(response);
                he.close();
            }
        }
    }
}