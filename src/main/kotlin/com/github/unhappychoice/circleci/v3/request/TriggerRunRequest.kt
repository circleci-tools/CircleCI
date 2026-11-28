package com.github.unhappychoice.circleci.v3.request

/** CircleCI enforces matching ref types when config and checkout use the same repository. */
class TriggerRunRequest(
    pipelineId: String,
    config: Ref? = null,
    checkout: Ref? = null,
    parameters: Map<String, Any>? = null
) {
    data class Ref(val branch: String? = null, val tag: String? = null) {
        init {
            require((branch != null) xor (tag != null)) { "Specify exactly one of branch or tag" }
        }
    }

    private val data = Data(Attributes(config, checkout, parameters), References(Pipeline(pipelineId)))

    private data class Data(val attributes: Attributes, val references: References)
    private data class Attributes(val config: Ref?, val checkout: Ref?, val parameters: Map<String, Any>?)
    private data class References(val pipeline: Pipeline)
    private data class Pipeline(val id: String)
}
