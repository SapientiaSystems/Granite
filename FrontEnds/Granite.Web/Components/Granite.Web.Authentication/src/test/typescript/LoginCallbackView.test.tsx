//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { StrictMode }                                          from "react";
import { describe, expect, it }                                from "vitest";
import { render, screen }                                      from "@testing-library/react";
import { MantineProvider }                                     from "@mantine/core";
import { MemoryRouter, Route, Routes }                         from "react-router-dom";
import type { ReactElement }                                   from "react";
import { AuthenticationProvider }                              from "../../main/typescript/Authentication/AuthenticationProvider";
import { LoginCallbackView }                                   from "../../main/typescript/Login/LoginCallbackView";
import { authenticationFor, KEYS, scriptedFrontEnd, sessionOutput } from "./TestSupport";
import type { FakeFrontEnd }                                   from "./TestSupport";

const routes: ReactElement =
    <Routes>
        <Route path="/login/callback" element={<LoginCallbackView />} />
        <Route path="/users" element={<div>users page</div>} />
        <Route path="/" element={<div>home page</div>} />
        <Route path="/login" element={<div>login page</div>} />
    </Routes>;

function renderCallback(frontEnd: FakeFrontEnd,query: string,strict: boolean = false): void
{
    const app: ReactElement =
        <MantineProvider>
            <AuthenticationProvider authentication={authenticationFor(frontEnd)}>
                <MemoryRouter initialEntries={["/login/callback" + query]}>{routes}</MemoryRouter>
            </AuthenticationProvider>
        </MantineProvider>;

    render(strict ? <StrictMode>{app}</StrictMode> : app);
}

describe("LoginCallbackView",() =>
{
    it("sends CompleteOktaLogin and navigates to the return path",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(true,"okta.user",["ROLE_Admin"],"/users"));

        renderCallback(frontEnd,"?code=c0de&state=st8");

        expect(await screen.findByText("users page")).toBeTruthy();
        expect(frontEnd.callsFor(KEYS.complete)[0].payload).toMatchObject({ code: "c0de", state: "st8" });
    });

    it("goes home when the server gives no return path",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(true,"okta.user",[]));

        renderCallback(frontEnd,"?code=c&state=s");

        expect(await screen.findByText("home page")).toBeTruthy();
    });

    it("completes once under StrictMode",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(true,"okta.user",[],"/users"));

        renderCallback(frontEnd,"?code=c&state=s",true);

        expect(await screen.findByText("users page")).toBeTruthy();
        expect(frontEnd.callsFor(KEYS.complete)).toHaveLength(1);
    });

    it("shows an alert and a way back when Okta reports an error",async () =>
    {
        const frontEnd = scriptedFrontEnd();

        renderCallback(frontEnd,"?error=access_denied&state=s");

        expect((await screen.findByRole("alert")).textContent).toContain("Sign-in was not completed");
        expect(screen.getByRole("link",{ name: "Back to sign in" })).toBeTruthy();
        expect(frontEnd.callsFor(KEYS.complete)).toHaveLength(0);
    });

    it("does not call the server without a code or state",async () =>
    {
        const frontEnd = scriptedFrontEnd();

        renderCallback(frontEnd,"?state=s");

        expect(await screen.findByRole("alert")).toBeTruthy();
        expect(frontEnd.callsFor(KEYS.complete)).toHaveLength(0);
    });

    it("shows an alert when the server refuses the code",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.complete,() => sessionOutput(false));

        renderCallback(frontEnd,"?code=c&state=s");

        expect((await screen.findByRole("alert")).textContent).toContain("Sign-in failed");
    });
});

//////////////////////////////////////////////////////////////////////////////
