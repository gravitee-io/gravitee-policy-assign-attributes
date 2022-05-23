/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.assignattributes;

import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.api.stream.BufferedReadWriteStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.SimpleReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AttributesToHeadersPolicy {

    @OnRequest
    public void onRequest(ExecutionContext context, Request request, Response response, PolicyChain policyChain) {
        transform(context, "test-request-", request.headers());

        policyChain.doNext(request, response);
    }

    @OnRequestContent
    public ReadWriteStream<Buffer> onRequestContent(ExecutionContext context, Request request) {
        return new BufferedReadWriteStream() {
            @Override
            public SimpleReadWriteStream<Buffer> write(Buffer content) {
                return this;
            }

            @Override
            public void end() {
                transform(context, "test-request-", request.headers());
                super.end();
            }
        };
    }

    @OnResponse
    public void onResponse(ExecutionContext context, Request request, Response response, PolicyChain policyChain) {
        transform(context, "test-response-", response.headers());

        policyChain.doNext(request, response);
    }

    @OnResponseContent
    public ReadWriteStream<Buffer> onResponseContent(ExecutionContext context, Response response) {
        return new BufferedReadWriteStream() {
            @Override
            public SimpleReadWriteStream<Buffer> write(Buffer content) {
                return this;
            }

            @Override
            public void end() {
                transform(context, "test-response-", response.headers());
                super.end();
            }
        };
    }

    private void transform(ExecutionContext context, String prefix, HttpHeaders request) {
        context
            .getAttributes()
            .forEach((key, value) -> {
                if (key.startsWith(prefix)) {
                    request.add(key, value.toString());
                }
            });
    }
}
