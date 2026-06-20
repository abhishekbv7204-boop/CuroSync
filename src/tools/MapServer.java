import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Tiny HTTP server that serves map.html and any static files
 * from the ada_project directory on port 7654.
 */
public class MapServer {
    static final String BASE = "c:/Users/user/Downloads/ada_project/ada_project";
    static final int PORT = 7654;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/map.html";
            File f = new File(BASE + path);
            if (!f.exists() || f.isDirectory()) {
                byte[] body = ("Not found: " + path).getBytes();
                exchange.sendResponseHeaders(404, body.length);
                exchange.getResponseBody().write(body);
                exchange.getResponseBody().close();
                return;
            }
            byte[] content = Files.readAllBytes(f.toPath());
            String ct = "text/plain";
            if (path.endsWith(".html")) ct = "text/html; charset=utf-8";
            else if (path.endsWith(".js"))   ct = "application/javascript";
            else if (path.endsWith(".css"))  ct = "text/css";
            else if (path.endsWith(".json")) ct = "application/json";
            exchange.getResponseHeaders().add("Content-Type", ct);
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
            exchange.getResponseBody().close();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("MapServer running on http://0.0.0.0:" + PORT);
        System.out.println("Open: http://localhost:" + PORT + "/map.html");
        // Keep alive
        Thread.currentThread().join();
    }
}
