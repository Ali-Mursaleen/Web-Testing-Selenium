package com.Abdul_Raqeeb.selenium;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple Spira REST helper that records a test-run in SpiraPlan (v6/v7 REST).
 *
 * Usage:
 *   SpiraMap.recordTestRun(
 *       "https://rmit.spiraservice.net",
 *       "your.spira.username",
 *       "{BA9FBA6F-41D2-4D1D-924A-36FCD757BFF2}",
 *       123,   // projectId
 *       45,    // testCaseId
 *       0,     // testSetId (optional; 0 means omit)
 *       0,     // releaseId (optional; 0 means omit)
 *       SpiraMap.Status.PASSED,
 *       "JUnit-Selenium",
 *       "LoginTest - executed by CI",
 *       "All assertions passed",
 *       ""
 *   );
 *
 * See Inflectra KB548: POST projects/{project_id}/test-runs/record
 */
public class SpiraMap {

    public enum Status {
        FAILED, PASSED, NOT_RUN, NOT_APPLICABLE, BLOCKED, CAUTION
    }

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    /**
     * Record a test run in Spira.
     *
     * @param spiraBaseUrl   base domain, e.g. "https://rmit.spiraservice.net"
     * @param username       Spira username (login)
     * @param apiKey         Spira API key (GUID)
     * @param projectId      numeric project id
     * @param testCaseId     numeric test case id
     * @param testSetId      test set id (0 to omit)
     * @param releaseId      release id (0 to omit)
     * @param status         Status enum (PASSED/FAILED/etc.)
     * @param runnerName     name of the test runner (e.g. "JUnit-5")
     * @param runnerTestName human friendly test name (e.g. "LoginTest.testValidLogin")
     * @param runnerMessage  short message (e.g. "All assertions passed")
     * @param runnerStack    stack trace if failed (or empty string)
     * @return JSON response from Spira (string)
     * @throws IOException on network/IO errors
     */
    public static String recordTestRun(
            String spiraBaseUrl,
            String username,
            String apiKey,
            int projectId,
            int testCaseId,
            int testSetId,
            int releaseId,
            Status status,
            String runnerName,
            String runnerTestName,
            String runnerMessage,
            String runnerStack
    ) throws IOException {

        if (spiraBaseUrl.endsWith("/")) {
            spiraBaseUrl = spiraBaseUrl.substring(0, spiraBaseUrl.length() - 1);
        }

        // build endpoint: /Services/v6_0/RestService.svc/projects/{projectId}/test-runs/record
        String endpoint = String.format("%s/Services/v6_0/RestService.svc/projects/%d/test-runs/record",
                spiraBaseUrl, projectId);

        // map Status -> ExecutionStatusId (Spira mapping)
        int executionStatusId = mapStatusToExecutionStatusId(status);

        // timestamp: start now, end now (for quick run) - ISO with timezone
        String now = ZonedDateTime.now().format(ISO_FMT);

        // Build JSON payload (simple; include fields required by Spira)
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"TestRunFormatId\":2,"); // 2 = automated runner format typical
        json.append("\"RunnerAssertCount\":0,");
        json.append("\"StartDate\":\"").append(now).append("\",");
        json.append("\"EndDate\":\"").append(now).append("\",");
        json.append("\"RunnerName\":\"").append(escapeJson(runnerName)).append("\",");
        json.append("\"RunnerTestName\":\"").append(escapeJson(runnerTestName)).append("\",");
        json.append("\"RunnerMessage\":\"").append(escapeJson(runnerMessage)).append("\",");
        json.append("\"RunnerStackTrace\":\"").append(escapeJson(runnerStack)).append("\",");
        json.append("\"TestCaseId\":").append(testCaseId).append(",");
        if (releaseId > 0) {
            json.append("\"ReleaseId\":").append(releaseId).append(",");
        }
        if (testSetId > 0) {
            json.append("\"TestSetId\":").append(testSetId).append(",");
        }
        json.append("\"ExecutionStatusId\":").append(executionStatusId);

        // close json
        json.append("}");

        // POST to Spira
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");

            // Basic Auth: username:apiKey
            String basic = username + ":" + apiKey;
            String encoded = Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encoded);

            // write body
            byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(body.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }

            // read response
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readStream(is);

            System.out.println("SpiraMap: POST " + endpoint + " -> HTTP " + code);
            System.out.println("SpiraMap: Request JSON: " + json.toString());
            System.out.println("SpiraMap: Response: " + response);

            if (code < 200 || code >= 300) {
                throw new IOException("Spira API returned HTTP " + code + " : " + response);
            }

            return response;
        } finally {
            conn.disconnect();
        }
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    private static int mapStatusToExecutionStatusId(Status status) {
        // Spira mapping (from documentation / REST metadata)
        // Failed = 1; Passed = 2; NotRun = 3; NotApplicable = 4; Blocked = 5; Caution = 6;
        switch (status) {
            case FAILED:
                return 1;
            case PASSED:
                return 2;
            case NOT_RUN:
                return 3;
            case NOT_APPLICABLE:
                return 4;
            case BLOCKED:
                return 5;
            case CAUTION:
                return 6;
            default:
                return 3;
        }
    }

    // minimal JSON escaping
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    // Convenience wrapper to be used from tests: indicates PASS/FAIL quickly
    public static String reportSimplePassFail(String spiraBaseUrl,
                                              String username,
                                              String apiKey,
                                              int projectId,
                                              int testCaseId,
                                              boolean passed,
                                              String testName,
                                              String message) {
        try {
            Status st = passed ? Status.PASSED : Status.FAILED;
            return recordTestRun(
                    spiraBaseUrl,
                    username,
                    apiKey,
                    projectId,
                    testCaseId,
                    0,
                    0,
                    st,
                    "JUnit-Selenium",
                    testName,
                    message,
                    passed ? "" : "See logs for details"
            );
        } catch (Exception e) {
            System.err.println("SpiraMap: Failed to post result: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Example: call this in an @AfterEach of a JUnit test
    public static void exampleUsageFromTest() {
        // DO NOT hardcode real credentials here in repo - use env vars or a local secrets file
        String spiraBase = "https://rmit.spiraservice.net";
        String user = System.getenv("SPIRA_USER");   // set in your environment
        String key = System.getenv("SPIRA_APIKEY");  // set in your environment
        int projectId = 766; // replace with actual project id
        int testCaseId = 123; // replace with actual test case id

        boolean passed = true; // set based on test result
        try {
            recordTestRun(spiraBase, user, key, projectId, testCaseId, 0, 0,
                    passed ? Status.PASSED : Status.FAILED, "JUnit-Selenium", "MyTest", "message", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
