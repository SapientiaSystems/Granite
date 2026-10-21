//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { beforeEach, describe, expect, it }                                from "vitest";
import { AuthenticationManager, REFRESH_MARGIN_MILLIS }                    from "../../main/typescript/Authentication/AuthenticationManager";
import { fakeApi, flush, grant, makeJwt, ManualTimer, MemoryTokenStorage } from "./TestSupport";
import type { IFakeSessionApi }                                            from "./TestSupport";

const NOW_SECONDS: number = 1_800_000_000;
const NOW_MILLIS:  number = NOW_SECONDS * 1000;

describe("AuthenticationManager",() =>
{
    let api:     IFakeSessionApi;
    let storage: MemoryTokenStorage;
    let timer:   ManualTimer;

    beforeEach(() =>
    {
        api     = fakeApi();
        storage = new MemoryTokenStorage();
        timer   = new ManualTimer(NOW_MILLIS);
    });

    it("starts anonymous without a stored session",() =>
    {
        expect(new AuthenticationManager(api,storage,timer).getState().status).toBe("anonymous");
    });

    it("starts restoring when a refresh token is stored",() =>
    {
        storage.stored = { refreshToken: "refresh-0", sessionId: 2002 };

        expect(new AuthenticationManager(api,storage,timer).getState().status).toBe("restoring");
    });

    it("login stores the refresh token and publishes the authenticated state",async () =>
    {
        const manager = new AuthenticationManager(api,storage,timer);
        const seen: Array<string> = [];

        manager.subscribe(state => seen.push(state.status));
        api.login.mockResolvedValue(grant(1,NOW_SECONDS + 900));

        await manager.login("alice","secret");

        expect(api.login).toHaveBeenCalledWith("alice","secret","device-1");
        expect(seen).toEqual(["authenticating","authenticated"]);
        expect(manager.getState()).toMatchObject({
            status:               "authenticated",
            userName:             "alice",
            userId:               1001,
            sessionId:            2002,
            authorities:          ["ROLE_USER"],
            accessTokenExpiresAt: (NOW_SECONDS + 900) * 1000,
        });
        expect(storage.stored).toEqual({ refreshToken: "refresh-1", sessionId: 2002, userName: "alice" });
    });

    it("failed login clears storage, reports the error and rethrows",async () =>
    {
        const manager = new AuthenticationManager(api,storage,timer);

        api.login.mockRejectedValue(new Error("invalid credentials"));

        await expect(manager.login("alice","wrong")).rejects.toThrow("invalid credentials");
        expect(manager.getState()).toMatchObject({ status: "anonymous", error: "invalid credentials" });
        expect(storage.stored).toBeNull();
    });

    it("refreshes one margin before the access token expires and rotates both tokens",async () =>
    {
        const manager = new AuthenticationManager(api,storage,timer);
        const expiry: number = NOW_SECONDS + 900;

        api.login.mockResolvedValue(grant(1,expiry));
        api.refresh.mockResolvedValue(grant(2,expiry + 900));
        await manager.login("alice","secret");

        timer.advanceTo(expiry * 1000 - REFRESH_MARGIN_MILLIS - 1);
        expect(api.refresh).not.toHaveBeenCalled();

        timer.advanceTo(expiry * 1000 - REFRESH_MARGIN_MILLIS);
        await flush();

        expect(api.refresh).toHaveBeenCalledWith("refresh-1");
        expect(storage.stored?.refreshToken).toBe("refresh-2");
        expect(manager.getState().accessTokenExpiresAt).toBe((expiry + 900) * 1000);
        expect(timer.pending).toHaveLength(1);
    });

    it("a failed refresh signs the user out",async () =>
    {
        const manager = new AuthenticationManager(api,storage,timer);

        api.login.mockResolvedValue(grant(1,NOW_SECONDS + 900));
        api.refresh.mockRejectedValue(new Error("refresh token reused; session revoked"));
        await manager.login("alice","secret");

        await manager.refreshNow();

        expect(manager.getState().status).toBe("anonymous");
        expect(storage.stored).toBeNull();
        expect(timer.pending).toHaveLength(0);
    });

    it("restore exchanges the stored refresh token and keeps the user name",async () =>
    {
        storage.stored = { refreshToken: "refresh-0", sessionId: 2002, userName: "alice" };
        api.refresh.mockResolvedValue(grant(1,NOW_SECONDS + 900));

        const manager = new AuthenticationManager(api,storage,timer);

        await manager.restore();

        expect(api.refresh).toHaveBeenCalledWith("refresh-0");
        expect(manager.getState()).toMatchObject({ status: "authenticated", userName: "alice" });
    });

    it("restore with a rejected refresh token ends anonymous",async () =>
    {
        storage.stored = { refreshToken: "stale", sessionId: 2002 };
        api.refresh.mockRejectedValue(new Error("invalid refresh token"));

        const manager = new AuthenticationManager(api,storage,timer);

        await manager.restore();

        expect(manager.getState().status).toBe("anonymous");
        expect(storage.stored).toBeNull();
    });

    it("logout clears local state first and then revokes the session",async () =>
    {
        const manager = new AuthenticationManager(api,storage,timer);
        const issued  = grant(1,NOW_SECONDS + 900);

        api.login.mockResolvedValue(issued);
        await manager.login("alice","secret");

        await manager.logout();

        expect(manager.getState().status).toBe("anonymous");
        expect(storage.stored).toBeNull();
        expect(timer.pending).toHaveLength(0);
        expect(api.logout).toHaveBeenCalledWith(2002,issued.accessToken);
    });

    it("google sign-in sends the ID token and shows the Google email",async () =>
    {
        const manager = new AuthenticationManager(api,storage,timer);
        const idToken = makeJwt({ email: "alice@example.com", sub: "google-sub" });

        api.loginWithOauth2.mockResolvedValue(grant(1,NOW_SECONDS + 900));

        await manager.loginWithGoogle(idToken);

        expect(api.loginWithOauth2).toHaveBeenCalledWith("google",idToken,"device-1");
        expect(manager.getState().userName).toBe("alice@example.com");
    });
});

//////////////////////////////////////////////////////////////////////////////
