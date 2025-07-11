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

import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.api.stream.BufferedReadWriteStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.SimpleReadWriteStream;
import io.gravitee.gateway.reactive.api.context.http.HttpMessageExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.message.Message;
import io.gravitee.gateway.reactive.api.policy.http.HttpPolicy;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AttributesToHeadersPolicy implements HttpPolicy {

    @Override
    public String id() {
        return "attributes-to-headers";
    }

    @Override
    public Completable onRequest(HttpPlainExecutionContext ctx) {
        return Completable.fromRunnable(() -> {
            transform(ctx, "test-request-", ctx.request().headers());
        });
    }

    @Override
    public Completable onResponse(HttpPlainExecutionContext ctx) {
        return Completable.fromRunnable(() -> {
            transform(ctx, "test-response-", ctx.response().headers());
        });
    }

    @Override
    public Completable onMessageRequest(HttpMessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> transformMessage(message, "test-message-request-"));
    }

    @Override
    public Completable onMessageResponse(HttpMessageExecutionContext ctx) {
        return ctx.response().onMessage(message -> transformMessage(message, "test-message-response-"));
    }

    @OnRequest
    public void onRequest(ExecutionContext context, Request request, Response response, PolicyChain policyChain) {
        transform(context, "test-request-", request.headers());

        policyChain.doNext(request, response);
    }

    @OnRequestContent
    public ReadWriteStream<Buffer> onRequestContent(ExecutionContext context, Request request) {
        return new AttributesToHeaderStream(context, "test-request-", request.headers());
    }

    @OnResponse
    public void onResponse(ExecutionContext context, Request request, Response response, PolicyChain policyChain) {
        transform(context, "test-response-", response.headers());

        policyChain.doNext(request, response);
    }

    @OnResponseContent
    public ReadWriteStream<Buffer> onResponseContent(ExecutionContext context, Response response) {
        return new AttributesToHeaderStream(context, "test-response-", response.headers());
    }

    static class AttributesToHeaderStream extends BufferedReadWriteStream {

        private final Buffer buffer = Buffer.buffer();
        private final ExecutionContext context;

        private final String prefix;
        private final HttpHeaders headers;

        AttributesToHeaderStream(ExecutionContext context, String prefix, HttpHeaders headers) {
            this.context = context;
            this.prefix = prefix;
            this.headers = headers;
        }

        @Override
        public SimpleReadWriteStream<Buffer> write(Buffer content) {
            buffer.appendBuffer(content);
            return this;
        }

        @Override
        public void end() {
            transform(context, prefix, headers);

            if (buffer.length() > 0) {
                super.write(buffer);
            }
            super.end();
        }
    }

    private static void transform(ExecutionContext context, String prefix, HttpHeaders headers) {
        context
            .getAttributes()
            .forEach((key, value) -> {
                if (key.startsWith(prefix)) {
                    headers.add(key, value.toString());
                }
            });
    }

    private void transform(HttpPlainExecutionContext context, String prefix, HttpHeaders headers) {
        context
            .getAttributes()
            .forEach((key, value) -> {
                if (key.startsWith(prefix)) {
                    headers.add(key, value.toString());
                }
            });
    }

    private Maybe<Message> transformMessage(Message message, String prefix) {
        return Maybe.fromCallable(() -> {
            message
                .attributes()
                .forEach((key, value) -> {
                    if (key.startsWith(prefix)) {
                        message.headers().add(key, value.toString());
                    }
                });
            return message;
        });
    }
}
