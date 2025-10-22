package com.Abdul_Raqeeb.selenium;

import org.junit.jupiter.api.Test;

public class SpiraSmokeTest {

    @Test
    public void testSpiraPost() {
        String spiraBase = "https://rmit.spiraservice.net";
        String user = System.getenv("SPIRA_USER");    // must be set
        String key  = System.getenv("SPIRA_APIKEY");  // must be set

        if (user == null || key == null) {
            System.out.println("ENV VARS MISSING: set SPIRA_USER and SPIRA_APIKEY in your run environment.");
            return;
        }

        int projectId = 766;    // confirm this from Spira URL
        int testCaseId = 12345; // REPLACE with a real test-case id from your project

        System.out.println("Sending test result to Spira...");
        String resp = SpiraMap.reportSimplePassFail(
                spiraBase, user, key,
                projectId, testCaseId,
                true,
                "SpiraSmokeTest",
                "Smoke-run from local machine"
        );

        System.out.println("SpiraMap returned: " + resp);
    }
}
