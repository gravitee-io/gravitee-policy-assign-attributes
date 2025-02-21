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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.reactive.api.context.ExecutionContext;
import io.gravitee.gateway.reactive.api.context.Request;
import io.gravitee.gateway.reactive.api.context.Response;
import io.gravitee.gateway.reactive.api.el.EvaluableRequest;
import io.gravitee.gateway.reactive.api.el.EvaluableResponse;
import io.gravitee.gateway.reactive.api.message.DefaultMessage;
import io.gravitee.gateway.reactive.api.message.Message;
import io.gravitee.gateway.reactive.core.context.MutableExecutionContext;
import io.gravitee.gateway.reactive.core.context.MutableRequest;
import io.gravitee.gateway.reactive.core.context.MutableResponse;
import io.gravitee.gateway.reactive.core.context.interruption.InterruptionFailureException;
import io.gravitee.policy.assignattributes.configuration.AssignAttributesPolicyConfiguration;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AssignAttributesPolicyTest {

    @Mock(extraInterfaces = MutableExecutionContext.class)
    private ExecutionContext ctx;

    @Mock(extraInterfaces = MutableRequest.class)
    private Request request;

    @Mock(extraInterfaces = MutableResponse.class)
    private Response response;

    @Spy
    private Completable spyCompletable = Completable.complete();

    private Message message;

    @BeforeEach
    public void init() {
        TemplateEngine templateEngine = TemplateEngine.templateEngine();

        HttpHeaders httpHeaders = HttpHeaders.create();
        httpHeaders.add("X-Gravitee-Test", "Value");
        httpHeaders.add("X-Gravitee-Test2", "Value2");

        //Ctx
        lenient().when(ctx.getTemplateEngine()).thenReturn(templateEngine);
        lenient().when(ctx.getTemplateEngine(any())).thenReturn(templateEngine);
        lenient().when(ctx.request()).thenReturn(request);
        lenient().when(ctx.response()).thenReturn(response);
        lenient().when(ctx.interruptWith(any())).thenAnswer(inv -> Completable.error(new InterruptionFailureException(inv.getArgument(0))));
        lenient()
            .when(ctx.interruptMessageWith(any()))
            .thenAnswer(inv -> Maybe.error(new InterruptionFailureException(inv.getArgument(0))));

        //Request
        lenient().when(request.headers()).thenReturn(httpHeaders);
        templateEngine.getTemplateContext().setVariable("request", new EvaluableRequest(request));
        lenient().when(request.onMessages(any())).thenReturn(spyCompletable);
        lenient().when(request.onMessage(any())).thenReturn(spyCompletable);

        //Response
        lenient().when(response.headers()).thenReturn(httpHeaders);
        templateEngine.getTemplateContext().setVariable("response", new EvaluableResponse(response));
        lenient().when(response.onMessages(any())).thenReturn(spyCompletable);
        lenient().when(response.onMessage(any())).thenReturn(spyCompletable);

        //Message
        message = DefaultMessage.builder().content("content").build();
        templateEngine.getTemplateContext().setVariable("message", message);
    }

    @Nested
    class onRequest {

        @Test
        public void should_not_assign_attribute_when_config_empty() {
            // Run
            policy(AssignAttributesPolicyConfiguration.builder().build()).onRequest(ctx).test().assertComplete();

            // Verify
            verify(ctx, never()).setAttribute(any(), any());
        }

        @Test
        public void should_add_attribute() {
            // Run
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Context-Attribute-Key", "{#request.headers['X-Gravitee-Test']}")))
                    .build()
            )
                .onRequest(ctx)
                .test()
                .assertComplete();

            // Verify
            verify(ctx).setAttribute(eq("Context-Attribute-Key"), any());
        }

        @Test
        public void should_add_attributes() {
            // Prepare
            List<Attribute> attributes = new LinkedList<>();
            attributes.add(new Attribute("Context-Attribute-Key", "{#request.headers['X-Gravitee-Test']}"));
            attributes.add(new Attribute("Context-Attribute-Key2", "{#request.headers['X-Gravitee-Test2']}"));

            // Run
            policy(AssignAttributesPolicyConfiguration.builder().attributes(attributes).build()).onRequest(ctx).test().assertComplete();

            // Verify
            verify(ctx, times(2)).setAttribute(any(), any());
        }

        @Test
        void should_not_interrupt_on_error() {
            // Prepare
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Context-Attribute-Key", "{#request.invalid}")))
                    .build()
            )
                .onRequest(ctx)
                .test()
                .assertComplete();

            // Verify
            verify(ctx, never()).setAttribute(any(), any());
        }
    }

    @Nested
    class onResponse {

        @Test
        public void should_not_assign_attribute_when_config_empty() {
            // Run
            policy(AssignAttributesPolicyConfiguration.builder().build()).onResponse(ctx).test().assertComplete();

            // Verify
            verify(ctx, never()).setAttribute(any(), any());
        }

        @Test
        public void should_add_attribute() {
            // Run
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Context-Attribute-Key", "{#response.headers['X-Gravitee-Test']}")))
                    .build()
            )
                .onResponse(ctx)
                .test()
                .assertComplete();

            // Verify
            verify(ctx).setAttribute(eq("Context-Attribute-Key"), any());
        }

        @Test
        public void should_add_attributes() {
            List<Attribute> attributes = new LinkedList<>();
            attributes.add(new Attribute("Context-Attribute-Key", "{#response.headers['X-Gravitee-Test']}"));
            attributes.add(new Attribute("Context-Attribute-Key2", "{#response.headers['X-Gravitee-Test2']}"));

            // Run
            policy(AssignAttributesPolicyConfiguration.builder().attributes(attributes).build()).onResponse(ctx).test().assertComplete();

            // Verify
            verify(ctx, times(2)).setAttribute(any(), any());
        }

        @Test
        void should_not_interrupt_on_error() {
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Context-Attribute-Key", "{#request.invalid}")))
                    .build()
            )
                .onResponse(ctx)
                .test()
                .assertComplete();

            // Verify
            verify(ctx, never()).setAttribute(any(), any());
        }
    }

    @Nested
    class onMessage {

        @Test
        public void should_add_attribute_to_message_request() {
            // Prepare
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Message-Attribute-Key", "{#message.content}")))
                    .build()
            )
                .onMessageRequest(ctx)
                .test()
                .assertComplete();

            ArgumentCaptor<Function<Message, Maybe<Message>>> requestMessagesCaptor = ArgumentCaptor.forClass(Function.class);
            verify(request).onMessage(requestMessagesCaptor.capture());

            // Run
            Function<Message, Maybe<Message>> requestMessages = requestMessagesCaptor.getValue();
            var messages = requestMessages.apply(message).test().assertComplete().values();

            // Verify
            assertThat(messages).hasSize(1);
            assertThat(messages).first().matches(message -> message.attribute("Message-Attribute-Key").toString().equals("content"));
        }

        @Test
        void should_add_attribute_to_message_response() {
            // Prepare
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Message-Attribute-Key", "{#message.content}")))
                    .build()
            )
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            ArgumentCaptor<Function<Message, Maybe<Message>>> responseMessagesCaptor = ArgumentCaptor.forClass(Function.class);
            verify(response).onMessage(responseMessagesCaptor.capture());

            // Run
            Function<Message, Maybe<Message>> responseMessages = responseMessagesCaptor.getValue();
            var messages = responseMessages.apply(message).test().assertComplete().values();

            // Verify
            assertThat(messages).hasSize(1);
            assertThat(messages).first().matches(message -> message.attribute("Message-Attribute-Key").toString().equals("content"));
        }

        @Test
        void should_not_interrupt_on_error() {
            // Prepare
            policy(
                AssignAttributesPolicyConfiguration
                    .builder()
                    .attributes(Collections.singletonList(new Attribute("Message-Attribute-Key", "{#message.invalid}")))
                    .build()
            )
                .onMessageResponse(ctx)
                .test()
                .assertComplete();

            ArgumentCaptor<Function<Message, Maybe<Message>>> responseMessagesCaptor = ArgumentCaptor.forClass(Function.class);
            verify(response).onMessage(responseMessagesCaptor.capture());

            // Run
            Function<Message, Maybe<Message>> responseMessages = responseMessagesCaptor.getValue();
            var messages = responseMessages.apply(message).test().assertComplete().values();

            // Verify
            assertThat(messages).hasSize(1);
            assertThat(messages).first().matches(message -> !message.attributeNames().contains("Message-Attribute-Key"));
        }
    }

    AssignAttributesPolicy policy(AssignAttributesPolicyConfiguration configuration) {
        return new AssignAttributesPolicy(configuration);
    }
}
