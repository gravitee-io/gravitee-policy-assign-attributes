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
package io.gravitee.policy.v3.assignattributes;

import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.el.EvaluableRequest;
import io.gravitee.gateway.api.el.EvaluableResponse;
import io.gravitee.gateway.api.stream.BufferedReadWriteStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.SimpleReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import io.gravitee.policy.assignattributes.Attribute;
import io.gravitee.policy.assignattributes.PolicyScope;
import io.gravitee.policy.assignattributes.configuration.AssignAttributesPolicyConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
public class AssignAttributesPolicyV3 {

    private static final String REQUEST_VARIABLE = "request";
    private static final String RESPONSE_VARIABLE = "response";

    protected final AssignAttributesPolicyConfiguration assignAttributesPolicyConfiguration;

    protected final boolean hasAttributes;

    public AssignAttributesPolicyV3(final AssignAttributesPolicyConfiguration configuration) {
        this.assignAttributesPolicyConfiguration = configuration;
        this.hasAttributes = configuration.getAttributes() != null && !configuration.getAttributes().isEmpty();
    }

    @OnRequestContent
    public ReadWriteStream onRequestContent(Request request, ExecutionContext executionContext) {
        if (
            assignAttributesPolicyConfiguration.getScope() != null &&
            assignAttributesPolicyConfiguration.getScope() == PolicyScope.REQUEST_CONTENT
        ) {
            return new BufferedReadWriteStream() {
                Buffer buffer = Buffer.buffer();

                @Override
                public SimpleReadWriteStream<Buffer> write(Buffer content) {
                    buffer.appendBuffer(content);
                    return this;
                }

                @Override
                public void end() {
                    String content = buffer.toString();
                    executionContext
                        .getTemplateEngine()
                        .getTemplateContext()
                        .setVariable(REQUEST_VARIABLE, new EvaluableRequest(request, content));

                    // assign
                    assign(executionContext);

                    if (buffer.length() > 0) {
                        super.write(buffer);
                    }

                    super.end();
                }
            };
        }

        return null;
    }

    @OnResponseContent
    public ReadWriteStream onResponseContent(Response response, ExecutionContext executionContext) {
        if (
            assignAttributesPolicyConfiguration.getScope() != null &&
            assignAttributesPolicyConfiguration.getScope() == PolicyScope.RESPONSE_CONTENT
        ) {
            return new BufferedReadWriteStream() {
                Buffer buffer = Buffer.buffer();

                @Override
                public SimpleReadWriteStream<Buffer> write(Buffer content) {
                    buffer.appendBuffer(content);
                    return this;
                }

                @Override
                public void end() {
                    String content = buffer.toString();
                    executionContext
                        .getTemplateEngine()
                        .getTemplateContext()
                        .setVariable(RESPONSE_VARIABLE, new EvaluableResponse(response, content));

                    // assign
                    assign(executionContext);

                    if (buffer.length() > 0) {
                        super.write(buffer);
                    }

                    super.end();
                }
            };
        }

        return null;
    }

    @OnRequest
    public void onRequest(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        if (
            assignAttributesPolicyConfiguration.getScope() == null || assignAttributesPolicyConfiguration.getScope() == PolicyScope.REQUEST
        ) {
            // assign
            assign(executionContext);
        }

        // continue chaining
        policyChain.doNext(request, response);
    }

    @OnResponse
    public void onResponse(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        if (
            assignAttributesPolicyConfiguration.getScope() != null && assignAttributesPolicyConfiguration.getScope() == PolicyScope.RESPONSE
        ) {
            // assign
            assign(executionContext);
        }

        // continue chaining
        policyChain.doNext(request, response);
    }

    private void assign(ExecutionContext executionContext) {
        if (hasAttributes) {
            assignAttributesPolicyConfiguration
                .getAttributes()
                .forEach(attribute -> {
                    if (checkAttributeNameAndValue(attribute)) {
                        try {
                            Object extValue = executionContext.getTemplateEngine().getValue(attribute.getValue(), Object.class);
                            if (extValue != null) {
                                executionContext.setAttribute(attribute.getName(), extValue);
                            }
                        } catch (Exception ex) {
                            log.error("An error occurs while decoding context attribute", ex);
                        }
                    }
                });
        }
    }

    protected boolean checkAttributeNameAndValue(Attribute attribute) {
        return attribute.getName() != null && !attribute.getName().trim().isEmpty() && attribute.getValue() != null;
    }
}
