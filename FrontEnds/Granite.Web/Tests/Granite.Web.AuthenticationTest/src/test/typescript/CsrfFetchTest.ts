//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi } from "vitest";
import { CsrfFetch }               from "granite.web.authentication/Http";

function response(status: number): Response
{
    return new Response("{}",{ status });
}

function headerOf(call: Array<unknown>): string | null
{
    return new Headers((call[1] as RequestInit | undefined)?.headers).get("X-XSRF-TOKEN");
}

describe("CsrfFetch",() =>
{
    it("sends the raw cookie value on a POST",async () =>
    {
        const send = vi.fn().mockResolvedValue(response(200));
        const csrf = new CsrfFetch(send,() => "tok/en=");

        await csrf.fetch("/x",{ method: "POST",body: "{}" });

        expect(headerOf(send.mock.calls[0])).toBe("tok/en=");
    });

    it.each(["PUT","PATCH","DELETE"])("sends the header on %s",async (method: string) =>
    {
        const send = vi.fn().mockResolvedValue(response(200));

        await new CsrfFetch(send,() => "t").fetch("/x",{ method });

        expect(headerOf(send.mock.calls[0])).toBe("t");
    });

    it("sends nothing on a GET",async () =>
    {
        const send = vi.fn().mockResolvedValue(response(200));

        await new CsrfFetch(send,() => "t").fetch("/x");

        expect(headerOf(send.mock.calls[0])).toBeNull();
    });

    it("sends nothing on a POST when there is no cookie",async () =>
    {
        const send = vi.fn().mockResolvedValue(response(200));

        await new CsrfFetch(send,() => undefined).fetch("/x",{ method: "POST" });

        expect(headerOf(send.mock.calls[0])).toBeNull();
    });

    it("retries once with the fresh cookie after a 403",async () =>
    {
        const send   = vi.fn().mockResolvedValueOnce(response(403)).mockResolvedValueOnce(response(200));
        const tokens = ["old","new"];
        const csrf   = new CsrfFetch(send,() => tokens.shift());

        const result = await csrf.fetch("/x",{ method: "POST",body: "{}" });

        expect(result.status).toBe(200);
        expect(send).toHaveBeenCalledTimes(2);
        expect(headerOf(send.mock.calls[1])).toBe("new");
    });

    it("retries when the first request had no cookie and the 403 set one",async () =>
    {
        const send   = vi.fn().mockResolvedValueOnce(response(403)).mockResolvedValueOnce(response(200));
        const tokens = [undefined,"fresh"];

        const result = await new CsrfFetch(send,() => tokens.shift()).fetch("/x",{ method: "POST" });

        expect(result.status).toBe(200);
        expect(headerOf(send.mock.calls[1])).toBe("fresh");
    });

    it("does not retry a 403 when the cookie is unchanged",async () =>
    {
        const send = vi.fn().mockResolvedValue(response(403));

        const result = await new CsrfFetch(send,() => "same").fetch("/x",{ method: "POST" });

        expect(result.status).toBe(403);
        expect(send).toHaveBeenCalledTimes(1);
    });

    it("returns a second 403",async () =>
    {
        const send = vi.fn().mockResolvedValue(response(403));
        let n      = 0;

        const result = await new CsrfFetch(send,() => "t" + n++).fetch("/x",{ method: "POST" });

        expect(result.status).toBe(403);
        expect(send).toHaveBeenCalledTimes(2);
    });
});

//////////////////////////////////////////////////////////////////////////////
