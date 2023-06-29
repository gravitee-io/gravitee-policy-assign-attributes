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

import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.configuration.GatewayConfigurationBuilder;
import io.gravitee.apim.gateway.tests.sdk.policy.PolicyBuilder;
import io.gravitee.definition.model.Api;
import io.gravitee.definition.model.ExecutionMode;
import io.gravitee.plugin.policy.PolicyPlugin;
import io.gravitee.policy.assignattributes.configuration.AssignAttributesPolicyConfiguration;
import java.util.Map;

@GatewayTest(v2ExecutionMode = ExecutionMode.V3)
public class V3EngineTest extends AbstractPolicyTest<AssignAttributesPolicy, AssignAttributesPolicyConfiguration> {

    @Override
    public void configurePolicies(Map<String, PolicyPlugin> policies) {
        // This policy will transform the attributes into headers to be able to test them.
        // on request phase: attributes must start with "test-request-"
        // on response phase: attributes must start with "test-response-"
        super.configurePolicies(policies);
        policies.put("attributes-to-headers", PolicyBuilder.build("attributes-to-headers", AttributesToHeadersPolicy.class));
    }
}
