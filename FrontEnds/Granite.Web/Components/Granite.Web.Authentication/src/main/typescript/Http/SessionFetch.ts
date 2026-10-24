//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { FetchFunction } from "./CsrfFetch";

export type UnauthorizedHandler = () => Promise<boolean>;

export class SessionFetch
{
    readonly fetch: FetchFunction;

    private readonly send: FetchFunction;
    private handler:       UnauthorizedHandler | undefined;

    constructor(csrfFetch: FetchFunction)
    {
        this.send    = csrfFetch;
        this.handler = undefined;
        this.fetch   = (input,init) => this.execute(input,init);
    }

    setUnauthorizedHandler(handler: UnauthorizedHandler): void
    {
        this.handler = handler;
    }

    private async execute(input: RequestInfo | URL,init?: RequestInit): Promise<Response>
    {
        const response: Response = await this.send(input,init);

        if (response.status !== 401 || !this.handler)
            return response;

        return await this.handler() ? this.send(input,init) : response;
    }
}

//////////////////////////////////////////////////////////////////////////////
