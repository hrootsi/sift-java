package com.siftscience;

import com.google.common.collect.Lists;
import com.siftscience.model.GetDecisionFieldSet;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static com.siftscience.model.GetDecisionFieldSet.AbuseType.ACCOUNT_ABUSE;
import static com.siftscience.model.GetDecisionFieldSet.AbuseType.ACCOUNT_TAKEOVER;
import static com.siftscience.model.GetDecisionFieldSet.EntityType.ORDER;
import static java.net.HttpURLConnection.HTTP_OK;

public class GetDecisionsTest {
    
    @Test
    public void testDecisionStatus() throws Exception {
        long createdBefore = System.currentTimeMillis();
        String accountId = "your_account_id";

        String responseBody = "{" +
                "   \"data\": [" +
                "       {" +
                "           \"id\": \"user_is_good\"," +
                "           \"name\": \"User is good\"," +
                "           \"description\": \"likely a good user\"," +
                "           \"category\": \"ACCEPT\"," +
                "           \"entity_type\": \"USER\"," +
                "           \"abuse_type\": \"LEGACY\"," +
                "           \"webhook_url\": \"\"," +
                "           \"created_at\": 123," +
                "           \"created_by\": \"clint@biz.com\"," +
                "           \"updated_at\": 234," +
                "           \"updated_by\":\"clint@biz.com\"" +
                "       }" +
                "   ]," +
                "   \"has_more\": false," +
                "   \"total_results\": 3," +
                "   \"next_ref\": \"/v3/accounts/" + accountId + "/decisions?created_before=" +
                        createdBefore + "\"" +
                "}";


        MockWebServer server = new MockWebServer();
        MockResponse response = new MockResponse();
        response.setResponseCode(HTTP_OK);

        response.setBody(responseBody);
        server.enqueue(response);
        server.start();
        HttpUrl baseUrl;
        baseUrl = server.url("");

        // Create a new client and link it to the mock server.
        SiftClient client = new SiftClient("your_api_key");
        client.setBaseApi3Url(baseUrl);

        // Build and execute the request against the mock server.
        GetDecisionsRequest getDecisionsRequest = client.buildRequest(new GetDecisionFieldSet()
                .setAccountId(accountId)
                .setLimit(11)
                .setAbuseTypes(Lists.newArrayList(ACCOUNT_ABUSE, ACCOUNT_TAKEOVER))
                .setCreatedBefore(createdBefore)
                .setEntityType(ORDER)

        );
        GetDecisionsResponse siftResponse = getDecisionsRequest.send();

        // Verify the request.
        RecordedRequest request = server.takeRequest();
        Assert.assertEquals("GET", request.getMethod());
        Assert.assertEquals("/v3/accounts/your_account_id/decisions?entity_type=ORDER&limit=11" +
                "&created_before="+createdBefore+"&abuse_types=ACCOUNT_ABUSE,ACCOUNT_TAKEOVER",
                request.getPath());
        Assert.assertEquals(request.getHeader("Authorization"), "Basic eW91cl9hcGlfa2V5Og==");

        // Verify the response was parsed correctly.
        Assert.assertEquals(HTTP_OK, siftResponse.getHttpStatusCode());
        JSONAssert.assertEquals(response.getBody().readUtf8(),
                siftResponse.getBody().toJson(), true);
    }
}