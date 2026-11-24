//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { afterEach, describe, expect, it, vi }               from "vitest";
import { FakeFrontEnd, KEYS, redirectOutput, sessionOutput } from "./TestSupport";

async function loadAuthentication(): Promise<typeof import("granite.web.authentication/Authentication")>
{
    vi.resetModules();

    return await import("granite.web.authentication/Authentication");
}

describe("ApplicationConfig", () =>
{
    afterEach(() =>
    {
        delete window.__GRANITE_CONFIG__;
    });

    it("defaultsToGranite", async () =>
    {
        const { APPLICATION } = await loadAuthentication();

        expect(APPLICATION).toBe("granite");
    });

    it("readsTheApplicationFromTheRuntimeConfig", async () =>
    {
        window.__GRANITE_CONFIG__ = { application: "client" };

        const { APPLICATION } = await loadAuthentication();

        expect(APPLICATION).toBe("client");
    });

    it("fallsBackToGraniteWhenBlank", async () =>
    {
        window.__GRANITE_CONFIG__ = { application: "  " };

        const { APPLICATION } = await loadAuthentication();

        expect(APPLICATION).toBe("granite");
    });

    it("sendsTheApplicationWithThePasswordLogin", async () =>
    {
        window.__GRANITE_CONFIG__ = { application: "client" };

        const { FrontEndSessionService } = await loadAuthentication();
        const frontEnd                   = new FakeFrontEnd().respond(KEYS.login,() => sessionOutput(true,"alice",["ROLE_CLIENT_ADMIN"]));

        await new FrontEndSessionService(() => frontEnd).login("alice","secret");

        expect(frontEnd.calls[0].payload).toMatchObject({ command: "Login", application: "client" });
    });

    it("sendsTheApplicationWithTheOktaStart", async () =>
    {
        const { FrontEndSessionService } = await loadAuthentication();
        const frontEnd                   = new FakeFrontEnd().respond(KEYS.start,() => redirectOutput("https://okta.example/a"));

        await new FrontEndSessionService(() => frontEnd).startOktaLogin("/");

        expect(frontEnd.calls[0].payload).toMatchObject({ command: "StartOktaLogin", application: "granite" });
    });
});

//////////////////////////////////////////////////////////////////////////////
