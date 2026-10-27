//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi }               from "vitest";
import { GetOutputRequestBuilder }                from "strata.frontendcontroller.service/RequestReply";
import { ServiceError }                           from "strata.client.core/Service";
import { GraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { CsrfFetch }                              from "granite.web.authentication/Http";

function jsonResponse(status: number,body: unknown): Response
{
    return new Response(JSON.stringify(body),{ status,headers: { "Content-Type": "application/json" } });
}

function request()
{
    return GetOutputRequestBuilder.newBuilder().setSource("Session.New").setDestination("LoginView").build();
}

describe("typed FrontEndController client over injected fetch",() =>
{
    it("posts JSON with the CSRF header and no Authorization",async () =>
    {
        const send   = vi.fn().mockResolvedValue(jsonResponse(200,{ success: true }));
        const csrf   = new CsrfFetch(send,() => "tok");
        const client = new GraniteFrontEndControllerServiceClient("/api/frontendcontroller-user-service",csrf.fetch);

        const reply      = await client.getSessionOutput(request());
        const [url,init] = send.mock.calls[0] as [string,RequestInit];
        const headers    = new Headers(init.headers);

        expect(reply.success).toBe(true);
        expect(url).toBe("/api/frontendcontroller-user-service/get-session-output");
        expect(init.method).toBe("POST");
        expect(headers.get("Content-Type")).toBe("application/json");
        expect(headers.get("X-XSRF-TOKEN")).toBe("tok");
        expect(headers.has("Authorization")).toBe(false);
        expect(JSON.parse(init.body as string).source).toBe("Session.New");
    });

    it("reads the headers before close clears them",async () =>
    {
        const send   = vi.fn().mockResolvedValue(jsonResponse(200,{}));
        const client = new GraniteFrontEndControllerServiceClient("/s",send);
        const stage  = client.getSessionOutput(request());

        client.close();
        await stage;

        expect(new Headers((send.mock.calls[0][1] as RequestInit).headers).get("Content-Type")).toBe("application/json");
    });

    it("resolves a 500 with its entity",async () =>
    {
        const send   = vi.fn().mockResolvedValue(jsonResponse(500,{ success: false,failureMessage: "boom" }));
        const client = new GraniteFrontEndControllerServiceClient("/s",send);

        expect((await client.getSessionOutput(request())).failureMessage).toBe("boom");
    });

    it("rejects a 401 with a ServiceError carrying the status",async () =>
    {
        const send   = vi.fn().mockResolvedValue(new Response("",{ status: 401 }));
        const client = new GraniteFrontEndControllerServiceClient("/s",send);

        const error = await client.getSessionOutput(request()).then(() => undefined,(failure: unknown) => failure);

        expect(error).toBeInstanceOf(ServiceError);
        expect((error as ServiceError).getStatus()).toBe(401);
    });
});

//////////////////////////////////////////////////////////////////////////////
