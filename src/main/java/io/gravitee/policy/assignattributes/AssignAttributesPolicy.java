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

import io.gravitee.gateway.reactive.api.context.base.BaseExecutionContext;
import io.gravitee.gateway.reactive.api.context.base.BaseMessageExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpMessageExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.message.Message;
import io.gravitee.gateway.reactive.api.policy.http.HttpPolicy;
import io.gravitee.policy.assignattributes.configuration.AssignAttributesPolicyConfiguration;
import io.gravitee.policy.v3.assignattributes.AssignAttributesPolicyV3;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
public class AssignAttributesPolicy extends AssignAttributesPolicyV3 implements HttpPolicy {

    private final Flowable<Attribute> attributeFlowable;

    public AssignAttributesPolicy(final AssignAttributesPolicyConfiguration configuration) {
        super(configuration);
        this.attributeFlowable =
            hasAttributes
                ? Flowable
                    .fromIterable(assignAttributesPolicyConfiguration.getAttributes())
                    .filter(this::checkAttributeNameAndValue)
                    .cache()
                : Flowable.empty();
    }

    @Override
    public String id() {
        return "policy-assign-attributes";
    }

    @Override
    public Completable onRequest(HttpPlainExecutionContext ctx) {
        return assign(ctx);
    }

    @Override
    public Completable onResponse(HttpPlainExecutionContext ctx) {
        return assign(ctx);
    }

    @Override
    public Completable onMessageRequest(HttpMessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> assign(ctx, message));
    }

    @Override
    public Completable onMessageResponse(HttpMessageExecutionContext ctx) {
        return ctx.response().onMessage(message -> assign(ctx, message));
    }

    private Completable assign(BaseExecutionContext executionContext) {
        if (hasAttributes) {
            return attributeFlowable
                .flatMapMaybe(attribute ->
                    executionContext
                        .getTemplateEngine()
                        .eval(attribute.getValue(), Object.class)
                        .doOnSuccess(extValue -> executionContext.setAttribute(attribute.getName(), extValue))
                        .doOnError(t -> log.error("An error occurs while decoding context attribute {}", t.getMessage()))
                        .onErrorComplete()
                )
                .ignoreElements();
        }
        return Completable.complete();
    }

    private Maybe<Message> assign(BaseMessageExecutionContext executionContext, Message message) {
        if (hasAttributes) {
            return attributeFlowable
                .flatMapMaybe(attribute ->
                    executionContext
                        .getTemplateEngine(message)
                        .eval(attribute.getValue(), Object.class)
                        .doOnSuccess(extValue -> message.attribute(attribute.getName(), extValue))
                        .onErrorComplete()
                )
                .ignoreElements()
                .andThen(Maybe.just(message));
        }
        return Maybe.just(message);
    }
}
