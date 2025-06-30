/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.assignattributes;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@GatewayTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AssignAttributesPolicyV3EngineIntegrationTest extends V3EngineTest {

    @Test
    @DisplayName("Should assign attributes (and convert them to headers for test purpose)")
    @DeployApi("/apis/v2/assign-attributes.json")
    void should_assign_attributes(HttpClient client) {
        wiremock.stubFor(get("/endpoint").willReturn(ok()));

        client
            .rxRequest(GET, "/test")
            .flatMap(HttpClientRequest::rxSend)
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().get("test-response-attr1")).isEqualTo("response-1");
                assertThat(response.headers().get("test-response-content")).isEqualTo("response-content");
                return response.toFlowable();
            })
            .test()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertComplete()
            .assertNoErrors();

        wiremock.verify(
            1,
            getRequestedFor(urlPathEqualTo("/endpoint"))
                .withHeader("test-request-attr1", equalTo("request-1"))
                .withHeader("test-request-content", equalTo("request-content"))
        );
    }

    @Test
    @DisplayName("Should assign attributes with EL (and convert them to headers for test purpose)")
    @DeployApi("/apis/v2/assign-attributes-with-el.json")
    void should_assign_attributes_with_EL(HttpClient client) {
        String requestContent = "request-content";
        String responseContent = "response-content";
        wiremock.stubFor(post("/endpoint").withRequestBody(equalTo(requestContent)).willReturn(ok(responseContent)));

        client
            .rxRequest(POST, "/test")
            .flatMap(request -> request.rxSend(Buffer.buffer(requestContent)))
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().get("test-response-content")).isEqualTo(responseContent);
                return response.toFlowable();
            })
            .test()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertNoErrors();

        wiremock.verify(1, postRequestedFor(urlPathEqualTo("/endpoint")).withHeader("test-request-content", equalTo(requestContent)));
    }

    @Test
    @DisplayName("Should assign attributes with EL (and convert them to headers for test purpose)")
    @DeployApi("/apis/v2/assign-multiple-attributes-with-el.json")
    void should_assign_multiple_attributes_with_EL(HttpClient client) {
        String requestContent =
            """
                                {
                                    "registrationForm": {
                                        "username": "username345",
                                        "firstName": "firstname123",
                                        "birthDate": "1928-04-20"
                                    }
                                }
                                """;
        String responseContent =
            """
                            {
                                "responseForm": {
                                    "username": "usernameResp345",
                                    "firstName": "firstnameResp123",
                                    "birthDate": "1928-04-20"
                                }
                            }
                            """;
        wiremock.stubFor(post("/endpoint").withRequestBody(equalTo(requestContent)).willReturn(ok(responseContent)));

        client
            .rxRequest(POST, "/testMultiple")
            .flatMap(request -> request.rxSend(Buffer.buffer(requestContent)))
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().get("test-response-content")).isEqualTo("firstnameResp123");
                assertThat(response.headers().get("test-response-content1")).isEqualTo("1928-04-20");
                assertThat(response.headers().get("test-response-content2")).isEqualTo("usernameResp345");
                return response.toFlowable();
            })
            .test()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertNoErrors();
        wiremock.verify(
            1,
            postRequestedFor(urlPathEqualTo("/endpoint"))
                .withHeader("test-request-content", equalTo("firstname123"))
                .withHeader("test-request-content1", equalTo("1928-04-20"))
                .withHeader("test-request-content2", equalTo("username345"))
        );
    }

    @Test
    @DisplayName("Should respond with 200 when applying an invalid EL")
    @DeployApi("/apis/v2/assign-attributes-with-error.json")
    void should_respond_with_200_when_applying_an_invalid_EL(HttpClient client) {
        String requestContent = "request-content";
        String responseContent = "response-content";
        wiremock.stubFor(post("/endpoint").withRequestBody(equalTo(requestContent)).willReturn(ok(responseContent)));

        client
            .rxRequest(POST, "/test")
            .flatMap(request -> request.rxSend(Buffer.buffer(requestContent)))
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return response.toFlowable();
            })
            .test()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertNoErrors();
    }

    @Test
    @DisplayName("Should respond with 200 when applying an invalid EL")
    @DeployApi("/apis/v2/assign-attributes-on-content-with-error.json")
    void should_respond_with_200_when_applying_an_invalid_EL_on_content(HttpClient client) {
        String requestContent = "request-content";
        String responseContent = "response-content";
        wiremock.stubFor(post("/endpoint").withRequestBody(equalTo(requestContent)).willReturn(ok(responseContent)));

        client
            .rxRequest(POST, "/test")
            .flatMap(request -> request.rxSend(Buffer.buffer(requestContent)))
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return response.toFlowable();
            })
            .test()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertNoErrors();
    }
}
