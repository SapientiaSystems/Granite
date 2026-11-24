//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }   from "vitest";
import { FrontEndSessionService } from "granite.web.authentication/Authentication";
import { FakeFrontEnd, KEYS, changePasswordOutput, logoutOutput, methodsOutput, redirectOutput, serviceError,
         sessionOutput }          from "./TestSupport";

function serviceOver(frontEnd: FakeFrontEnd): FrontEndSessionService
{
    return new FrontEndSessionService(() => frontEnd);
}

describe("FrontEndSessionService",() =>
{
    it("reads the login methods",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.methods,() => methodsOutput(false,true));

        expect(await serviceOver(frontEnd).getLoginMethods()).toEqual({ passwordEnabled: false, oktaEnabled: true });
        expect(frontEnd.calls[0].payload).toMatchObject({ source: "Session.New", destination: "LoginView" });
        expect(frontEnd.calls[0].payload.userId ?? null).toBeNull();
    });

    it("sends the password login input",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.login,() => sessionOutput(true,"alice",["ROLE_USER"]));

        const status = await serviceOver(frontEnd).login("alice","secret");

        expect(status).toMatchObject({ authenticated: true, userName: "alice", authorities: ["ROLE_USER"] });
        expect(frontEnd.calls[0].payload).toEqual(
            { userId: null, source: "Session.New", command: "Login", destination: "LoginView", userName: "alice", password: "secret", application: "granite" });
    });

    it("maps UNAUTHENTICATED to a refusal",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.login,() => sessionOutput(false));

        expect((await serviceOver(frontEnd).login("a","b")).authenticated).toBe(false);
    });

    it("starts Okta and returns the redirect URL",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.start,() => redirectOutput("https://okta.example/a"));

        expect(await serviceOver(frontEnd).startOktaLogin("/users")).toBe("https://okta.example/a");
        expect(frontEnd.calls[0].payload).toEqual(
            { userId: null, source: "Session.New", command: "StartOktaLogin", destination: "LoginView", returnTo: "/users", application: "granite" });
    });

    it("returns no URL when Okta start is refused",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.start,() => sessionOutput(false));

        expect(await serviceOver(frontEnd).startOktaLogin("/")).toBeUndefined();
    });

    it("sends the Okta completion input",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.complete,() => sessionOutput(true,"u",[],"/x"));

        const status = await serviceOver(frontEnd).completeOktaLogin("code-1","state-1");

        expect(status.returnTo).toBe("/x");
        expect(frontEnd.calls[0].payload).toEqual(
            { userId: null, source: "Session.New", command: "CompleteOktaLogin", destination: "LoginView", code: "code-1", state: "state-1" });
    });

    it("reads the current session",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.current,() => sessionOutput(true,"alice",["ROLE_ADMIN"]));

        expect(await serviceOver(frontEnd).getCurrent()).toMatchObject({ authenticated: true, userName: "alice" });
        expect(frontEnd.calls[0].payload).toMatchObject({ source: "Session.Current", destination: "SessionView" });
    });

    it("maps a 401 on the current session to unauthenticated",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.current,() => serviceError(401));

        expect((await serviceOver(frontEnd).getCurrent()).authenticated).toBe(false);
    });

    it("rethrows other failures on the current session",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.current,() => serviceError(502));

        await expect(serviceOver(frontEnd).getCurrent()).rejects.toThrow();
    });

    it("sends the logout input and returns only the logged-out flag",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.logout,() => ({ ...logoutOutput(true),endSessionUrl: "https://okta.example/end" }));

        expect(await serviceOver(frontEnd).logout()).toEqual({ loggedOut: true });
        expect(frontEnd.calls[0].payload).toEqual(
            { userId: null, source: "Session.Current", command: "Logout", destination: "LoginView" });
    });

    it("sends the change password input and maps CHANGED",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.change,() => changePasswordOutput(true));

        expect(await serviceOver(frontEnd).changePassword("old","new-password")).toEqual({ success: true });
        expect(frontEnd.calls[0].payload).toEqual(
            { userId: null, source: "Session.Current", command: "ChangePassword", destination: "ChangePasswordView", currentPassword: "old", newPassword: "new-password" });
    });

    it("maps FAILED to the first failure message",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.change,() => changePasswordOutput(false,"invalid credentials"));

        expect(await serviceOver(frontEnd).changePassword("old","new-password")).toEqual({ success: false, message: "invalid credentials" });
    });

    it("maps FAILED without a message to the generic message",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.change,() => changePasswordOutput(false));

        expect(await serviceOver(frontEnd).changePassword("a","b")).toEqual({ success: false, message: "Unable to change password" });
    });

    it("maps a thrown error to the generic message",async () =>
    {
        const frontEnd = new FakeFrontEnd().respond(KEYS.change,() => serviceError(500));

        expect(await serviceOver(frontEnd).changePassword("a","b")).toEqual({ success: false, message: "Unable to change password" });
    });

    it("rejects with the failure message when the reply is not successful",async () =>
    {
        const failing = new FakeFrontEnd();

        failing.handleSessionInput = () => Promise.resolve({ success: false, failureMessage: "down" }) as never;

        await expect(serviceOver(failing).login("a","b")).rejects.toThrow("down");
    });
});

//////////////////////////////////////////////////////////////////////////////
