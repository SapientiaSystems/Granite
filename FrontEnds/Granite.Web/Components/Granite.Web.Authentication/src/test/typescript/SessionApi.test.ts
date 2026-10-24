//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { beforeEach, describe, expect, it, vi } from "vitest";
import { SessionApi, toGrant }                  from "../../main/typescript/Authentication/SessionApi";
import { decodeJwtClaims, expiresAtMillis }     from "../../main/typescript/Authentication/JwtClaims";
import { getRuntimeConfig }                     from "../../main/typescript/Authentication/RuntimeConfig";
import { makeJwt }                              from "./TestSupport";

const client = vi.hoisted(() => ({ baseUrl: "", reply: undefined as unknown, error: undefined as Error | undefined, close: vi.fn() }));

vi.mock("granite.session.client/RequestReply",async () =>
{
    const { CompletableObservable } = await import("strata.foundation.core/Concurrent");

    return {
        SessionCommandServiceClient: class
        {
            constructor(baseUrl: string)
            {
                client.baseUrl = baseUrl;
            }

            login()
            {
                return client.error !== undefined
                    ? CompletableObservable.fromError(client.error)
                    : CompletableObservable.fromResult(client.reply);
            }

            close()
            {
                client.close();
            }
        },
    };
});

describe("SessionApi",() =>
{
    beforeEach(() =>
    {
        client.reply = undefined;
        client.error = undefined;
        client.close.mockClear();
    });

    it("login completes with the grant and closes the client",async () =>
    {
        client.reply = { success: true, accessToken: "access", refreshToken: "refresh",
                         sessionData: { sessionId: 7, userId: 9, authorities: ["ROLE_ADMIN"] } };

        await expect(new SessionApi("https://granite.example").login("alice","secret","device-1"))
            .resolves.toEqual({ accessToken: "access", refreshToken: "refresh", sessionId: 7, userId: 9, authorities: ["ROLE_ADMIN"] });
        expect(client.baseUrl).toBe("https://granite.example/session-command-service");
        expect(client.close).toHaveBeenCalledTimes(1);
    });

    it("login rejects with the Session failure message and closes the client",async () =>
    {
        client.reply = { success: false, failureMessage: "invalid credentials" };

        await expect(new SessionApi("https://granite.example").login("alice","wrong","device-1"))
            .rejects.toThrow("invalid credentials");
        expect(client.close).toHaveBeenCalledTimes(1);
    });

    it("login rejects and closes the client when the request fails",async () =>
    {
        client.error = new Error("network down");

        await expect(new SessionApi("https://granite.example").login("alice","secret","device-1"))
            .rejects.toThrow("network down");
        expect(client.close).toHaveBeenCalledTimes(1);
    });
});

describe("toGrant",() =>
{
    it("maps a successful Session reply",() =>
    {
        expect(
            toGrant({
                success:      true,
                accessToken:  "access",
                refreshToken: "refresh",
                sessionData:  { sessionId: 7, userId: 9, authorities: ["ROLE_ADMIN"] },
            }))
            .toEqual({ accessToken: "access", refreshToken: "refresh", sessionId: 7, userId: 9, authorities: ["ROLE_ADMIN"] });
    });

    it("throws the Session failure message",() =>
    {
        expect(() => toGrant({ success: false, failureMessage: "invalid credentials" })).toThrow("invalid credentials");
    });

    it("rejects a successful reply without a refresh token",() =>
    {
        expect(() => toGrant({ success: true, accessToken: "a", sessionData: { sessionId: 1 } })).toThrow();
    });
});

describe("JwtClaims",() =>
{
    it("decodes base64url claims including non-ASCII text",() =>
    {
        const token = makeJwt({ exp: 1_800_000_000, name: "Zoë" });

        expect(decodeJwtClaims(token).name).toBe("Zoë");
        expect(expiresAtMillis(token)).toBe(1_800_000_000_000);
    });

    it("returns no claims for malformed tokens",() =>
    {
        expect(decodeJwtClaims("not-a-jwt")).toEqual({});
        expect(expiresAtMillis("a.%%%.c")).toBeUndefined();
    });
});

describe("getRuntimeConfig",() =>
{
    it("reads window.__GRANITE_CONFIG__ and trims trailing slashes",() =>
    {
        window.__GRANITE_CONFIG__ = { apiBaseUrl: "https://granite.example/", googleClientId: "client-1" };

        expect(getRuntimeConfig()).toEqual({ apiBaseUrl: "https://granite.example", googleClientId: "client-1" });

        delete window.__GRANITE_CONFIG__;
        expect(getRuntimeConfig()).toEqual({ apiBaseUrl: "", googleClientId: "" });
    });
});

//////////////////////////////////////////////////////////////////////////////
