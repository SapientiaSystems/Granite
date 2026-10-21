//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }             from "vitest";
import { toGrant }                          from "../../main/typescript/Authentication/SessionApi";
import { decodeJwtClaims, expiresAtMillis } from "../../main/typescript/Authentication/JwtClaims";
import { getRuntimeConfig }                 from "../../main/typescript/Authentication/RuntimeConfig";
import { makeJwt }                          from "./TestSupport";

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
