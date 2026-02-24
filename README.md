
<!-- GENERATED CODE - DO NOT ALTER THIS OR THE FOLLOWING LINES -->
# Assign attributes

[![Gravitee.io](https://img.shields.io/static/v1?label=Available%20at&message=Gravitee.io&color=1EC9D2)](https://download.gravitee.io/#graviteeio-apim/plugins/policies/gravitee-policy-policy-assign-attributes/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gravitee-io/gravitee-policy-policy-assign-attributes/blob/master/LICENSE.txt)
[![Releases](https://img.shields.io/badge/semantic--release-conventional%20commits-e10079?logo=semantic-release)](https://github.com/gravitee-io/gravitee-policy-policy-assign-attributes/releases)
[![CircleCI](https://circleci.com/gh/gravitee-io/gravitee-policy-policy-assign-attributes.svg?style=svg)](https://circleci.com/gh/gravitee-io/gravitee-policy-policy-assign-attributes)

## Overview
You can use the `policy-assign-attributes` policy to set variables such as request attributes, message attributes, and other execution context attributes.

> **NOTE**: When you use this policy on message request or message response phases, attributes are stored in the message attribute list instead of the context attribute list.

This policy is useful to keep initial request data after policies such as `Transform headers` or `Transform query parameters`, and reuse these values in downstream policies like `Dynamic routing`.




## Phases
The `policy-assign-attributes` policy can be applied to the following API types and flow phases.

### Compatible API types

* `PROXY`
* `MESSAGE`
* `NATIVE KAFKA`
* `MCP PROXY`
* `LLM PROXY`
* `A2A PROXY`

### Supported flow phases:

* Request
* Response
* Publish
* Subscribe
* Interact

## Compatibility matrix
Strikethrough text indicates that a version is deprecated.

| Plugin version| APIM |
| --- | ---  |
|3.x|4.8.x to latest |
|2.x|4.0.x to 4.7.x |
|~~1.x~~|~~3.x~~ |


## Configuration options


#### 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Description  |
|:----------------------|:-----------------------|:----------:|:-------------|
| Assign context attributes<br>`attributes`| array|  | <br/>See "Assign context attributes" section.|


#### Assign context attributes (Array)
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Description  |
|:----------------------|:-----------------------|:----------:|:-------------|
| Name<br>`name`| string| ✅| Name of the attribute.|
| Value<br>`value`| string| ✅| Value of the attribute (Support EL).|




## Examples

*Proxy API on Request phase*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Assign attributes example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Assign attributes",
            "enabled": true,
            "policy": "policy-assign-attributes",
            "configuration":
              {
                  "scope": "REQUEST_CONTENT",
                  "attributes": [
                      {
                          "name": "test-request-content",
                          "value": "{#jsonPath(#request.content, '$.registrationForm.firstName')}"
                      },
                      {
                          "name": "test-request-content1",
                          "value": "{#jsonPath(#request.content, '$.registrationForm.birthDate')}"
                      },
                      {
                          "name": "test-request-content2",
                          "value": "{#jsonPath(#request.content, '$.registrationForm.username')}"
                      }
                  ]
              }
          }
        ]
      }
    ]
  }
}

```
*Proxy API on Response phase*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Assign attributes example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "response": [
          {
            "name": "Assign attributes",
            "enabled": true,
            "policy": "policy-assign-attributes",
            "configuration":
              {
                  "scope": "RESPONSE_CONTENT",
                  "attributes": [
                      {
                          "name": "test-response-content",
                          "value": "{#jsonPath(#response.content, '$.responseForm.firstName')}"
                      },
                      {
                          "name": "test-response-content1",
                          "value": "{#jsonPath(#response.content, '$.responseForm.birthDate')}"
                      },
                      {
                          "name": "test-response-content2",
                          "value": "{#jsonPath(#response.content, '$.responseForm.username')}"
                      }
                  ]
              }
          }
        ]
      }
    ]
  }
}

```
*Message API CRD on Subscribe phase*
```yaml
apiVersion: "gravitee.io/v1alpha1"
kind: "ApiV4Definition"
metadata:
    name: "policy-assign-attributes-message-api-crd"
spec:
    name: "Assign attributes example"
    type: "MESSAGE"
    flows:
      - name: "Common Flow"
        enabled: true
        selectors:
            matchRequired: false
            mode: "DEFAULT"
        subscribe:
          - name: "Assign attributes"
            enabled: true
            policy: "policy-assign-attributes"
            configuration:
              attributes:
                  - name: test-message-response-attr1
                    value: '{#message.content}'

```


## Changelog

### [3.2.0](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/3.1.0...3.2.0) (2026-02-18)


##### Features

* enable for A2A proxy ([ec4a9ab](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/ec4a9ab60af02b272d8c5cbcc96c095fe662c818))

### [3.1.0](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/3.0.2...3.1.0) (2025-12-11)


##### Features

* enable for LLM & MCP Proxy API ([cc71b88](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/cc71b886717b5fcfcd3f44494d719b3197d161d5))

### [3.1.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/3.0.2...3.1.0-alpha.1) (2025-11-12)


##### Features

* enable for LLM & MCP Proxy API ([731bf8f](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/731bf8fcf9175222ecd0779bb1a2aad22706a6bb))

#### [3.0.2](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/3.0.1...3.0.2) (2025-09-01)


##### Bug Fixes

* use this policy on request/response phases ([c4e4e35](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/c4e4e3514dff43e3fbb655c66d9491e319689d49))

#### [3.0.1](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/3.0.0...3.0.1) (2025-07-02)


##### Bug Fixes

* changed flatMapMaybe to concatMapMaybe ([31e7d81](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/31e7d81387795051ac2d02cc1fd0f03910982ae3))

### [3.0.0](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.3...3.0.0) (2025-06-17)


##### chore

* make policy compatible with apim 4.8.0 ([424cf3e](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/424cf3e92679bf5d61ba65ec8e6b304fa1e103e8))


##### Features

* enable assign attribute policy for native apis ([619b191](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/619b19172f6062e766c600d8b093669b72d22638))


##### BREAKING CHANGES

* require APIM 4.8.0+ to work

#### [2.0.3](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.2...2.0.3) (2024-09-13)


##### Bug Fixes

* json-schema - remove json forced language ([2eac825](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/2eac825093016607c34eb07d07f5dde2d321e863))

#### [2.0.2](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.1...2.0.2) (2024-09-11)


##### Bug Fixes

* json-schema - add code editor for attribute values ([9fa4a86](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/9fa4a86a3badb7e9c88b4c44b3641fa586ceedfe))

#### [2.0.1](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.0...2.0.1) (2023-07-20)


##### Bug Fixes

* update policy description ([723e2ca](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/723e2ca31d5dc5ed9b37ba2416093a8a1de8ef64))

### [2.0.0](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/1.5.1...2.0.0) (2023-07-18)


##### Bug Fixes

* add missing breaking change and update compatibility matrix in README ([791eac8](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/791eac8fda525b107de08066595b5d3a59f8404a))
* bump gravitee-parent to fix release to nexus ([e4d0957](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/e4d0957b8a163b33936e784930d08c314bbea9ae))
* use new execution engine ([21ea3f7](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/21ea3f757bfe3cd059be7e08f06a1e945f2056fa))


##### chore

* **deps:** update gravitee-parent ([bd51e10](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/bd51e10421397a468347c82acf2e99e66e6c9102))


##### Features

* add message level support to policy ([676fc6f](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/676fc6fca791edd28607b101f5c5009e6c66e9b0))
* clean and validate json schema for v4 ([f915aca](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/f915acaf7cc90c16fe726b4f947bba4a56f76406))


##### BREAKING CHANGES

* **deps:** require Java17
* This implementation is using the dependencies introduced by Gravitee V4.0

### [2.0.0-alpha.4](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.0-alpha.3...2.0.0-alpha.4) (2023-06-29)


##### Bug Fixes

* use new execution engine ([21ea3f7](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/21ea3f757bfe3cd059be7e08f06a1e945f2056fa))

### [2.0.0-alpha.3](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.0-alpha.2...2.0.0-alpha.3) (2023-06-27)


##### Features

* clean and validate json schema for v4 ([f915aca](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/f915acaf7cc90c16fe726b4f947bba4a56f76406))

### [2.0.0-alpha.2](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/2.0.0-alpha.1...2.0.0-alpha.2) (2023-06-23)


##### Bug Fixes

* bump gravitee-parent to fix release to nexus ([e4d0957](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/e4d0957b8a163b33936e784930d08c314bbea9ae))

### [2.0.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/1.6.0-alpha.1...2.0.0-alpha.1) (2023-06-21)


##### Bug Fixes

* add missing breaking change and update compatibility matrix in README ([791eac8](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/791eac8fda525b107de08066595b5d3a59f8404a))


##### BREAKING CHANGES

* This implementation is using the dependencies introduced by Gravitee V4.0

### [1.6.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/1.5.1...1.6.0-alpha.1) (2023-06-20)


##### Features

* add message level support to policy ([676fc6f](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/676fc6fca791edd28607b101f5c5009e6c66e9b0))

#### [1.5.1](https://github.com/gravitee-io/gravitee-policy-assign-attributes/compare/1.5.0...1.5.1) (2023-01-23)


##### Bug Fixes

* properly set `response` attribute in the execution context ([f8d4de4](https://github.com/gravitee-io/gravitee-policy-assign-attributes/commit/f8d4de452eee650c0c8372394e9bc376ca87dbb3))

