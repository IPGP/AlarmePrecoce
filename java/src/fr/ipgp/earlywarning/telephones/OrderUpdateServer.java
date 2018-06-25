package fr.ipgp.earlywarning.telephones;

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
import java.util.List;

/**
 * A Web server used to maintain and update the call priority list for the Early Warning Alarm.
 */
public class OrderUpdateServer {
    /**
     * The default port for the Web server
     */
    private static final int DEFAULT_PORT = 6001;
    /**
     * Contact list
     */
    private static JSONContactList JSONContactList;
    /**
     * The port used by the server
     */
    private final int port;
    /**
     * The WWW home for the Web server's files
     */
    private final String home;

    public OrderUpdateServer(int port, String home, String contactsFile) throws IOException {
        this.port = port;
        this.home = home;

        JSONContactList = new JSONContactList(contactsFile);
    }

    /**
     * Constructs the server with the default port (DEFAULT_PORT) {@link OrderUpdateServer}
     *
     * @param home the WWW home for the Web server
     */
    public OrderUpdateServer(String home, String contactsFile) throws IOException {
        this(DEFAULT_PORT, home, contactsFile);
    }

    public int getPort()
    {
        return port;
    }

    /**
     * Starts the server.
     */
    public void startServer() throws IOException {
        EarlyWarning.appLogger.debug("Starting contact list Web server on port " + String.valueOf(port));
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
     * The handler to send files (html, js, etc.)
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
            } catch (IOException e) {
                EarlyWarning.appLogger.error(e.getMessage());
            }
        }

        /**
         * Generates the main page from the empty index.html and serves it
         *
         * @param file         the index.html empty template
         * @param responseBody the OutputStream to write to
         * @throws IOException if index.html can't be read
         */
        private void serveIndex(File file, OutputStream responseBody) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

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
            script.html("var people = " + JSONContactList.getAvailableContactsAsJson() + ";");
            script.html(script.html() + "var people_enabled = " + JSONContactList.getEnabledContactsAsJson() + ";");
            // Add the <script> to the <head>
            head.appendChild(script);
            // Write the completed template to the OutputStream
            responseBody.write(doc.toString().getBytes());
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

            String path = home + uri;
            if (path.endsWith("/"))
                path = path + "index.html";

            File f = new File(path);
            if (f.isFile()) {
                t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                if (path.endsWith("index.html"))
                    serveIndex(f, t.getResponseBody());
                else
                    sendFile(new FileInputStream(f), t.getResponseBody());
                t.getResponseBody().close();
            } else {
                EarlyWarning.appLogger.warn("File not found: '" + home + uri + "'");

                t.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 3);

                OutputStream os = t.getResponseBody();
                os.write("404".getBytes());
                os.close();
            }
        }
    }

    /**
     * The handler for order and enable / disable update requests.
     */
    static class PostHandler implements HttpHandler {
        private static void handleNewContact(JSONObject data) {
            String phone = data.getString("phone");
            String name = data.getString("name");
            boolean priority = data.getBoolean("priority");

            EarlyWarning.appLogger.debug("Adding contact " + name + " / " + phone);
            JSONContactList.addContact(new Contact(name, phone, priority), true);

            try {
                JSONContactList.write();
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Can't write JSONContactList: " + e.getMessage());
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

            List<String> names = new ArrayList<>();
            for (int i = 0; i < enabledNames.length(); i++) {
                names.add(enabledNames.getString(i));
            }
            JSONContactList.updateCallList(names);

            names.clear();
            for (int i = 0; i < allNames.length(); i++)
                names.add(allNames.getString(i));
            JSONContactList.clean(names);

            try {
                JSONContactList.write();
            } catch (IOException ex) {
                EarlyWarning.appLogger.error("Can't write contact list.");
            }
        }

        /**
         * Handles a POST request.
         *
         * @param he the exchange to handle
         * @throws IOException if the index.html template can't be read
         */
        public void handle(HttpExchange he) throws IOException {
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
                    System.out.println(new String(data));
                    JSONObject json = new JSONObject(new String(data));

                    if (uri.equalsIgnoreCase("/new"))
                        handleNewContact(json);
                    else if (uri.equalsIgnoreCase("/update"))
                        handleUpdate(json);
                    else {
                        byte[] response = "{\"status\" : \"error\"}".getBytes();
                        he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, response.length);
                        OutputStream os = he.getResponseBody();
                        os.write(response);
                        he.close();
                        return;
                    }

                    // Send a confirmation that everything went well.
                    byte[] response = "{\"status\" : \"success\"}".getBytes();
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    OutputStream os = he.getResponseBody();
                    os.write(response);
                    he.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // If we get a request that is not POST, send a BAD_METHOD status with a JSON content.
                EarlyWarning.appLogger.warn("Wrong request on " + he.getRequestURI().toString() + " and method " + he.getRequestMethod());
                byte[] response = "{\"status\" : \"Bad method\"}".getBytes();
                he.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length);
                he.getResponseBody().write(response);
                he.close();
            }
        }
    }
}