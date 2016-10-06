package com.siftscience;

import com.siftscience.model.PaymentMethod;
import com.siftscience.model.UpdateAccountFieldSet;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class UpdateAccountEventTest {
    @Test
    public void testUpdateAccount() throws Exception {
        String expectedRequestBody = "\n" +
                "{\n" +
                "  \"$type\"       : \"$update_account\",\n" +
                "  \"$api_key\"    : \"your_api_key_here\",\n" +
                "  \"$user_id\"    : \"billy_jones_301\",\n" +
                "  \"$changed_password\" : true,\n" +
                "  \"$user_email\"       : \"bill@gmail.com\",\n" +
                "  \"$name\"             : \"Bill Jones\",\n" +
                "  \"$phone\"            : \"1-415-555-6040\",\n" +
                "  \"$referrer_user_id\" : \"janejane102\",\n" +
                "  \"$payment_methods\"  : [\n" +
                "      {\n" +
                "          \"$payment_type\"    : \"$credit_card\",\n" +
                "          \"$card_bin\"        : \"542486\",\n" +
                "          \"$card_last4\"      : \"4444\"\n" +
                "      }\n" +
                "  ],\n" +
                "  \"$billing_address\"  : {\n" +
                "      \"$name\"         : \"Bill Jones\",\n" +
                "      \"$phone\"        : \"1-415-555-6041\",\n" +
                "      \"$address_1\"    : \"2100 Main Street\",\n" +
                "      \"$address_2\"    : \"Apt 3B\",\n" +
                "      \"$city\"         : \"New London\",\n" +
                "      \"$region\"       : \"New Hampshire\",\n" +
                "      \"$country\"      : \"US\",\n" +
                "      \"$zipcode\"      : \"03257\"\n" +
                "  },\n" +
                "  \"$shipping_address\"  : {\n" +
                "      \"$name\"          : \"Bill Jones\",\n" +
                "      \"$phone\"         : \"1-415-555-6041\",\n" +
                "      \"$address_1\"     : \"2100 Main Street\",\n" +
                "      \"$address_2\"     : \"Apt 3B\",\n" +
                "      \"$city\"          : \"New London\",\n" +
                "      \"$region\"        : \"New Hampshire\",\n" +
                "      \"$country\"       : \"US\",\n" +
                "      \"$zipcode\"       : \"03257\"\n" +
                "  },\n" +
                "  \"$social_sign_on_type\"   : \"$twitter\",\n" +
                "  \"email_confirmed_status\"  : \"$success\",\n" +
                "  \"phone_confirmed_status\"  : \"$success\"\n" +
                "}";

        // Start a new mock server and enqueue a mock response.
        MockWebServer server = new MockWebServer();
        MockResponse response = new MockResponse();
        response.setResponseCode(HTTP_OK);
        response.setBody("{\n" +
                "    \"status\" : 0,\n" +
                "    \"error_message\" : \"OK\",\n" +
                "    \"time\" : 1327604222,\n" +
                "    \"request\" : \"" + TestUtils.unescapeJson(expectedRequestBody) + "\"\n" +
                "}");
        server.enqueue(response);
        server.start();
        HttpUrl baseUrl = server.url("");

        // Create a new client and link it to the mock server.
        SiftClient client = new SiftClient("your_api_key_here");
        client.setBaseUrl(baseUrl);

        // Payment methods.
        List<PaymentMethod> paymentMethodList = new ArrayList<>();
        paymentMethodList.add(TestUtils.samplePaymentMethod2());

        // Build and execute the request against the mock server.
        SiftRequest request = client.buildEventRequest(
                new UpdateAccountFieldSet()
                        .setUserId("billy_jones_301")
                        .setUserEmail("bill@gmail.com")
                        .setChangedPassword(true)
                        .setName("Bill Jones")
                        .setPhone("1-415-555-6040")
                        .setReferrerUserId("janejane102")
                        .setPaymentMethods(paymentMethodList)
                        .setBillingAddress(TestUtils.sampleAddress2())
                        .setShippingAddress(TestUtils.sampleAddress2())
                        .setSocialSignOnType("$twitter")
                        .setCustomField("email_confirmed_status", "$success")
                        .setCustomField("phone_confirmed_status", "$success"));

        SiftResponse siftResponse = request.send();

        // Verify the request.
        RecordedRequest request1 = server.takeRequest();
        Assert.assertEquals("POST", request1.getMethod());
        Assert.assertEquals("/v204/events", request1.getPath());
        JSONAssert.assertEquals(expectedRequestBody, request.getFieldSet().toJson(), true);

        // Verify the response.
        Assert.assertEquals(HTTP_OK, siftResponse.getHttpStatusCode());
        Assert.assertEquals(0, (int) siftResponse.getResponseBody().getStatus());
        JSONAssert.assertEquals(response.getBody().readUtf8(),
                siftResponse.getResponseBody().toJson(), true);

        server.shutdown();
    }
}
