import {IAgentCommandServiceClient} from "./IAgentCommandServiceClient";
import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {AbstractRestClient} from "strata.client.core/Service";
import {Guid} from "guid-typescript";
import {AbstractServiceRequest} from "strata.foundation.core/Transfer";
import {Instant} from "@js-joda/core";
import {
    ResolveAgentActionReply,
    ResolveAgentActionRequest,
    SendAgentMessageReply,
    SendAgentMessageRequest
} from "granite.agent.service/RequestReply";

export
class AgentCommandServiceClient
    extends AbstractRestClient
    implements IAgentCommandServiceClient
{
    constructor(baseUrl: string)
    {
        super(baseUrl);
        this.setHeader("Content-Type","application/json");
        this.setHeader("Access-Control-Allow-Origin","*");
    }

    setAuthorization(accessToken: string): IAgentCommandServiceClient
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

    sendAgentMessage(request: SendAgentMessageRequest): ICompletionStage<SendAgentMessageReply>
    {
        return this.doPost<SendAgentMessageReply,SendAgentMessageRequest>(
            "send-agent-message",
            this.initializeRequest(request));
    }

    resolveAgentAction(request: ResolveAgentActionRequest): ICompletionStage<ResolveAgentActionReply>
    {
        return this.doPost<ResolveAgentActionReply,ResolveAgentActionRequest>(
            "resolve-agent-action",
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
