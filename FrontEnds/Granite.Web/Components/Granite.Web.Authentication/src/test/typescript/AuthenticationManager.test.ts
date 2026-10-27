//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                                    from "vitest";
import { AuthenticationManager }                                                   from "../../main/typescript/Authentication/AuthenticationManager";
import { FakeNavigator, KEYS, logoutOutput, managerFor, redirectOutput,
         scriptedFrontEnd, serviceError, sessionOutput, flush }                    from "./TestSupport";

describe("AuthenticationManager",() =>
{
    it("starts restoring and becomes anonymous when SessionView says unauthenticated",async () =>
    {
        const manager = managerFor(scriptedFrontEnd());

        expect(manager.getState().status).toBe("restoring");

        await manager.restore();

        expect(manager.getState().status).toBe("anonymous");
    });

    it("restores an authenticated session",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.current,() => sessionOutput(true,"alice",["ROLE_ADMIN"]));
        const manager  = managerFor(frontEnd);

        await manager.restore();

        expect(manager.getState()).toMatchObject({ status: "authenticated", userName: "alice", authorities: ["ROLE_ADMIN"] });
    });

    it("treats a 401 from SessionView as anonymous",async () =>
    {
        const manager = managerFor(scriptedFrontEnd().respond(KEYS.current,() => serviceError(401)));

        await manager.restore();

        expect(manager.getState().status).toBe("anonymous");
    });

    it("becomes anonymous with an error when SessionView cannot be reached",async () =>
    {
        const manager = managerFor(scriptedFrontEnd().respond(KEYS.current,() => new Error("offline")));

        await manager.restore();

        expect(manager.getState()).toMatchObject({ status: "anonymous", error: "offline" });
    });

    it("signs in with a password",async () =>
    {
        const frontEnd = scriptedFrontEnd();
        const manager  = managerFor(frontEnd);

        await manager.login("alice","secret");

        expect(manager.getState()).toMatchObject({ status: "authenticated", userName: "alice", authorities: ["ROLE_USER"] });
        expect(frontEnd.callsFor(KEYS.login)[0].payload).toMatchObject({ userName: "alice", password: "secret" });
    });

    it("refuses a wrong password without saying why",async () =>
    {
        const manager = managerFor(scriptedFrontEnd());

        await manager.restore();
        await expect(manager.login("alice","nope")).rejects.toThrow("Sign-in failed");
        expect(manager.getState()).toMatchObject({ status: "anonymous", error: "Sign-in failed" });
    });

    it("sends the user to Okta with the return path",async () =>
    {
        const frontEnd  = scriptedFrontEnd().respond(KEYS.start,() => redirectOutput("https://okta.example/authorize?x=1"));
        const navigator = new FakeNavigator();

        await managerFor(frontEnd,navigator).startOktaLogin("/users");

        expect(navigator.assigned).toEqual(["https://okta.example/authorize?x=1"]);
        expect(frontEnd.callsFor(KEYS.start)[0].payload).toMatchObject({ returnTo: "/users" });
    });

    it("throws when Okta start is refused",async () =>
    {
        const frontEnd  = scriptedFrontEnd().respond(KEYS.start,() => sessionOutput(false));
        const navigator = new FakeNavigator();

        await expect(managerFor(frontEnd,navigator).startOktaLogin("/")).rejects.toThrow("Sign-in failed");
        expect(navigator.assigned).toEqual([]);
    });

    it("completes the Okta callback and returns the local path",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(true,"okta.user",["ROLE_ADMIN"],"/users?tab=1"));
        const manager  = managerFor(frontEnd);

        expect(await manager.completeOktaLogin("c0de","st8")).toBe("/users?tab=1");
        expect(manager.getState().userName).toBe("okta.user");
        expect(frontEnd.callsFor(KEYS.complete)[0].payload).toMatchObject({ code: "c0de", state: "st8" });
    });

    it.each(["https://evil.example","//evil.example","/\\evil.example","users",""])("ignores a non-local returnTo (%s)",async (returnTo: string) =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(true,"okta.user",[],returnTo));

        expect(await managerFor(frontEnd).completeOktaLogin("c","s")).toBe("/");
    });

    it("rejects a failed Okta completion",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(false));

        await expect(managerFor(frontEnd).completeOktaLogin("c","s")).rejects.toThrow("Sign-in failed");
    });

    it("logs out and goes anonymous",async () =>
    {
        const frontEnd = scriptedFrontEnd();
        const manager  = managerFor(frontEnd);

        await manager.login("alice","secret");

        expect(await manager.logout()).toBe(true);
        expect(manager.getState().status).toBe("anonymous");
        expect(frontEnd.callsFor(KEYS.logout)[0].payload).toMatchObject({ source: "Session.Current", command: "Logout" });
    });

    it("navigates to the Okta end-session URL after logout",async () =>
    {
        const frontEnd  = scriptedFrontEnd().respond(KEYS.logout,() => logoutOutput(true,"https://okta.example/logout?id_token_hint=x"));
        const navigator = new FakeNavigator();

        await managerFor(frontEnd,navigator).logout();

        expect(navigator.assigned).toEqual(["https://okta.example/logout?id_token_hint=x"]);
    });

    it("keeps the session when logout fails",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.logout,() => logoutOutput(false));
        const manager  = managerFor(frontEnd);

        await manager.login("alice","secret");

        expect(await manager.logout()).toBe(false);
        expect(manager.getState().status).toBe("authenticated");
    });

    it("treats a 401 on logout as already signed out",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.logout,() => serviceError(401));
        const manager  = managerFor(frontEnd);

        await manager.login("alice","secret");

        expect(await manager.logout()).toBe(true);
        expect(manager.getState().status).toBe("anonymous");
    });
});

describe("AuthenticationManager.handleUnauthorized",() =>
{
    async function signedIn(frontEnd = scriptedFrontEnd()): Promise<AuthenticationManager>
    {
        const manager = managerFor(frontEnd);

        await manager.login("alice","secret");
        return manager;
    }

    it("retries without a prompt when SessionView shows another tab signed in",async () =>
    {
        const frontEnd = scriptedFrontEnd();
        const manager  = await signedIn(frontEnd);

        frontEnd.respond(KEYS.current,() => sessionOutput(true,"alice",["ROLE_USER"]));

        expect(await manager.handleUnauthorized()).toBe(true);
        expect(manager.getState().status).toBe("authenticated");
    });

    it("prompts, then resolves true after a password sign-in",async () =>
    {
        const manager = await signedIn();
        const result  = manager.handleUnauthorized();

        await flush();
        expect(manager.getState()).toMatchObject({ status: "reauthenticating", userName: "alice" });

        await manager.login("alice","secret");

        expect(await result).toBe(true);
        expect(manager.getState().status).toBe("authenticated");
    });

    it("stays in the prompt after a refused password",async () =>
    {
        const manager = await signedIn();

        void manager.handleUnauthorized();
        await flush();

        await expect(manager.login("alice","nope")).rejects.toThrow();
        expect(manager.getState().status).toBe("reauthenticating");
    });

    it("shares one prompt between concurrent 401s",async () =>
    {
        const frontEnd = scriptedFrontEnd();
        const manager  = await signedIn(frontEnd);
        const before   = frontEnd.callsFor(KEYS.current).length;
        const first    = manager.handleUnauthorized();
        const second   = manager.handleUnauthorized();

        await flush();
        await manager.login("alice","secret");

        expect(await first).toBe(true);
        expect(await second).toBe(true);
        expect(frontEnd.callsFor(KEYS.current).length - before).toBe(1);
    });

    it("resolves false and signs out when the prompt is cancelled",async () =>
    {
        const manager = await signedIn();
        const result  = manager.handleUnauthorized();

        await flush();
        manager.cancelReauthentication();

        expect(await result).toBe(false);
        expect(manager.getState().status).toBe("anonymous");
    });

    it("resolves false for a caller that was never signed in",async () =>
    {
        const manager = managerFor(scriptedFrontEnd());

        await manager.restore();

        expect(await manager.handleUnauthorized()).toBe(false);
        expect(manager.getState().status).toBe("anonymous");
    });
});

//////////////////////////////////////////////////////////////////////////////
