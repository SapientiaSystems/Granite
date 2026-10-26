//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import { AbstractFrontEndControllerServiceClient }     from "strata.frontendcontroller.client/RequestReply";
import { StandardResponse, StandardResponseProcessor } from "strata.client.core/Service";
import { CompletableObservable }                       from "strata.foundation.core/Concurrent";
import type { ICompletionStage }                       from "strata.foundation.core/Concurrent";
import type { FetchFunction }                          from "./FetchFunction";

export
abstract class AbstractGraniteFrontEndControllerServiceClient
    extends AbstractFrontEndControllerServiceClient
{
    private readonly serviceUrl:     string;
    private readonly fetchFunction?: FetchFunction;

    constructor(baseUrl: string,fetchFunction?: FetchFunction)
    {
        super(baseUrl);
        this.serviceUrl    = baseUrl;
        this.fetchFunction = fetchFunction;
    }

    protected override doPost<TReply,TRequest>(methodPath: string,request: TRequest): ICompletionStage<TReply>
    {
        const send: FetchFunction | undefined = this.fetchFunction;

        if (send === undefined)
            return super.doPost<TReply,TRequest>(methodPath,request);

        const headers: Headers = new Headers();

        for (const key of this.getHeaderKeys())
            headers.set(key,this.getHeader(key));

        const pending: Promise<TReply> =
            send(
                    this.serviceUrl + "/" + methodPath,
                    { method: "POST", mode: "cors", credentials: "same-origin", headers, body: JSON.stringify(request) })
                .then(response => new StandardResponseProcessor().process(new StandardResponse(this.serviceUrl,methodPath,response)) as Promise<TReply>);

        return CompletableObservable.fromPromise(pending);
    }
}

//////////////////////////////////////////////////////////////////////////////
