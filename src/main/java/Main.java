import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        int port = 8989;

        executeServer(port, engine);
    }

    public static void executeServer(int port, BooleanSearchEngine engine) throws IOException {
        out.println("Starting server at " + port + "...");

        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port);
                 Socket clientSocket = serverSocket.accept();
                 PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    String searchWord = in.readLine();
                    List<PageEntry> searchResult = engine.search(searchWord);

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    String response = searchResult.stream()
                                            .map(gson::toJson)
                                            .collect(Collectors.joining());

                    out.println(response);
                }
            }
    }
}
