//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi } from "vitest";
import { SessionFetch }            from "granite.web.authentication/Http";

function response(status: number): Response
{
    return new Response("{}",{ status });
}

describe("SessionFetch",() =>
{
    it("retries once when the handler confirms the session",async () =>
    {
        const send    = vi.fn().mockResolvedValueOnce(response(401)).mockResolvedValueOnce(response(200));
        const session = new SessionFetch(send);

        session.setUnauthorizedHandler(() => Promise.resolve(true));

        expect((await session.fetch("/x",{ method: "POST" })).status).toBe(200);
        expect(send).toHaveBeenCalledTimes(2);
    });

    it("returns the 401 when the handler declines",async () =>
    {
        const send    = vi.fn().mockResolvedValue(response(401));
        const session = new SessionFetch(send);

        session.setUnauthorizedHandler(() => Promise.resolve(false));

        expect((await session.fetch("/x")).status).toBe(401);
        expect(send).toHaveBeenCalledTimes(1);
    });

    it("returns the 401 when there is no handler",async () =>
    {
        const send = vi.fn().mockResolvedValue(response(401));

        expect((await new SessionFetch(send).fetch("/x")).status).toBe(401);
    });

    it("returns a second 401 without asking again",async () =>
    {
        const send    = vi.fn().mockResolvedValue(response(401));
        const handler = vi.fn().mockResolvedValue(true);
        const session = new SessionFetch(send);

        session.setUnauthorizedHandler(handler);

        expect((await session.fetch("/x")).status).toBe(401);
        expect(send).toHaveBeenCalledTimes(2);
        expect(handler).toHaveBeenCalledTimes(1);
    });

    it("never asks the handler after a 200",async () =>
    {
        const handler = vi.fn();
        const session = new SessionFetch(vi.fn().mockResolvedValue(response(200)));

        session.setUnauthorizedHandler(handler);
        await session.fetch("/x");

        expect(handler).not.toHaveBeenCalled();
    });
});

//////////////////////////////////////////////////////////////////////////////
