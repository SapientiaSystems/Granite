import {IAgentQueryServiceClient} from "./IAgentQueryServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    FindAgentConversationReply,
    FindAgentConversationRequest,
    FindAgentConversationsReply,
    FindAgentConversationsRequest
} from "granite.agent.service/RequestReply";

export
class AgentQueryServiceClient
    extends AbstractRestClient
    implements IAgentQueryServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IAgentQueryServiceClient
    {
        if (super.hasHeader("Authorization"))
            super.clearHeader("Authorization");

        super.setHeader("Authorization","Bearer " + accessToken);
        return this;
    }

    close(): void
    {
        super.clearHeaders();
    }

    findAgentConversations(request: FindAgentConversationsRequest): ICompletionStage<FindAgentConversationsReply>
    {
        return this.doPost<FindAgentConversationsReply,FindAgentConversationsRequest>(
            "find-agent-conversations",
            this.initializeRequest(request));
    }

    findAgentConversation(request: FindAgentConversationRequest): ICompletionStage<FindAgentConversationReply>
    {
        return this.doPost<FindAgentConversationReply,FindAgentConversationRequest>(
            "find-agent-conversation",
            this.initializeRequest(request));
    }

    private initializeRequest<R extends AbstractServiceRequest>(request: R): R
    {
        if (request.requestId == null)
            request.requestId = Guid.create().toString();

        if (request.timestamp == null)
            request.timestamp = Instant.now().toString();

        return request;
    }
}
