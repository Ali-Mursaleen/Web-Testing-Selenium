package com.Abdul_Raqeeb.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;

public class SpiraReporter {
    private static String SPIRA_BASE;
    private static String API_KEY;
    private static String PROJECT_ID;
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        try (InputStream in = SpiraReporter.class.getResourceAsStream("/config.properties")) {
            Properties p = new Properties();
            p.load(in);
            SPIRA_BASE = p.getProperty("spira.base");
            API_KEY = p.getProperty("spira.api.key");
            PROJECT_ID = p.getProperty("spira.project.id");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NOTE: adapt endpoint/payload to your Spira API. This is a generic placeholder.
    public static void sendResult(int testCaseId, boolean passed, String notes) {
        try {
            String endpoint = SPIRA_BASE + "/api/v1/test-runs"; // adapt if needed
            Map<String,Object> body = Map.of(
                    "project_id", Integer.parseInt(PROJECT_ID),
                    "test_case_id", testCaseId,
                    "status", passed ? "Passed" : "Failed",
                    "notes", notes
            );
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type","application/json")
                    .header("Authorization","Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("Spira response: " + resp.statusCode() + " - " + resp.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
