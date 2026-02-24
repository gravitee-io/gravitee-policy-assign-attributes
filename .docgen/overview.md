You can use the `{{ .Plugin.ID }}` policy to set variables such as request attributes, message attributes, and other execution context attributes.

> **NOTE**: When you use this policy on message request or message response phases, attributes are stored in the message attribute list instead of the context attribute list.

This policy is useful to keep initial request data after policies such as `Transform headers` or `Transform query parameters`, and reuse these values in downstream policies like `Dynamic routing`.
