//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { readCookie } from "./Cookies";

export type FetchFunction = (input: RequestInfo | URL,init?: RequestInit) => Promise<Response>;

export const CSRF_COOKIE: string = "XSRF-TOKEN";
export const CSRF_HEADER: string = "X-XSRF-TOKEN";

const UNSAFE_METHODS: ReadonlySet<string> = new Set(["POST","PUT","PATCH","DELETE"]);

export class CsrfFetch
{
    readonly fetch: FetchFunction;

    private readonly send:       FetchFunction;
    private readonly readToken:  () => string | undefined;

    constructor(
        fetchFunction: FetchFunction                = (input,init) => window.fetch(input,init),
        cookieReader:  (name: string) => string | undefined = (name: string) => readCookie(name))
    {
        this.send      = fetchFunction;
        this.readToken = () => cookieReader(CSRF_COOKIE);
        this.fetch     = (input,init) => this.execute(input,init);
    }

    private async execute(input: RequestInfo | URL,init?: RequestInit): Promise<Response>
    {
        const method: string = (init?.method ?? "GET").toUpperCase();

        if (!UNSAFE_METHODS.has(method))
            return this.send(input,init);

        const token:    string | undefined = this.readToken();
        const response: Response           = await this.send(input,withToken(init,token));

        if (response.status !== 403)
            return response;

        const fresh: string | undefined = this.readToken();

        if (fresh === undefined || fresh === token)
            return response;

        return this.send(input,withToken(init,fresh));
    }
}

function withToken(init: RequestInit | undefined,token: string | undefined): RequestInit | undefined
{
    if (token === undefined)
        return init;

    const headers: Headers = new Headers(init?.headers);

    headers.set(CSRF_HEADER,token);

    return { ...init, headers };
}

//////////////////////////////////////////////////////////////////////////////
