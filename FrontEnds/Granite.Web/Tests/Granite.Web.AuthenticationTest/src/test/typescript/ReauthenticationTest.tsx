//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { beforeEach, describe, expect, it, vi }     from "vitest";
import { screen, waitFor, within }                  from "@testing-library/react";
import userEvent                                    from "@testing-library/user-event";
import { Route, Routes }                            from "react-router-dom";
import { useState }                                 from "react";
import type { ReactElement }                        from "react";
import { GetOutputRequestBuilder }                  from "strata.frontendcontroller.service/RequestReply";
import { GraniteFrontEndControllerServiceClient }   from "granite.frontendcontroller.client/RequestReply";
import { useAuthentication, RequireAuthentication } from "granite.web.authentication/Authentication";
import { LoginView }                                from "granite.web.authentication/Login";
import { authenticationFor, FakeNavigator, KEYS, methodsOutput, redirectOutput, scriptedFrontEnd,
         sessionOutput }                            from "./TestSupport";
import type { FakeFrontEnd }                        from "./TestSupport";
import { renderWithAuthentication }                 from "./RenderSupport";

function jsonResponse(status: number,body: unknown = {}): Response
{
    return new Response(JSON.stringify(body),{ status,headers: { "Content-Type": "application/json" } });
}

function Caller(): ReactElement
{
    const { createFrontEndClient } = useAuthentication();
    const [result,setResult]       = useState<string>("idle");

    const load = (): void =>
    {
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),"frontendcontroller-user-service")
            .getUsersOutput(GetOutputRequestBuilder.newBuilder().setSource("User.All").setDestination("UsersEditableView").build())
            .then(reply => setResult(reply.success ? "loaded" : "refused"),() => setResult("failed"));
    };

    return <div><button onClick={load}>Load</button><span>{result}</span></div>;
}

const routes: ReactElement =
    <Routes>
        <Route path="/login" element={<LoginView />} />
        <Route path="/users" element={<RequireAuthentication><Caller /></RequireAuthentication>} />
    </Routes>;

interface IRig
{
    frontEnd: FakeFrontEnd;
    send:     ReturnType<typeof vi.fn>;
    session:  { alive: boolean };
    navigator: FakeNavigator;
}

function startRig(statuses: Array<number>): IRig
{
    const session   = { alive: true };
    const navigator = new FakeNavigator();
    const frontEnd  = scriptedFrontEnd()
        .respond(KEYS.current,() => session.alive ? sessionOutput(true,"alice",["ROLE_USER"]) : sessionOutput(false))
        .respond(KEYS.login,(payload) => payload.password === "secret" ? sessionOutput(true,"alice",["ROLE_USER"]) : sessionOutput(false))
        .respond(KEYS.methods,() => methodsOutput(true,true))
        .respond(KEYS.start,() => redirectOutput("https://okta.example/authorize"));
    const queue     = [...statuses];
    const send      = vi.fn().mockImplementation(() => Promise.resolve(jsonResponse(queue.shift() ?? 200,{ success: true })));

    renderWithAuthentication(authenticationFor(frontEnd,send,navigator),"/users",routes);

    return { frontEnd, send, session, navigator };
}

describe("re-authentication",() =>
{
    beforeEach(() => window.history.pushState({},"","/"));

    async function loadWithExpiredSession(rig: IRig): Promise<void>
    {
        const load = await screen.findByRole("button",{ name: "Load" });

        rig.session.alive = false;
        await userEvent.click(load);
    }

    it("retries the failed request once, with no Authorization, after the password dialog",async () =>
    {
        const rig = startRig([401,200]);

        await loadWithExpiredSession(rig);

        const dialog = await screen.findByRole("dialog");

        await userEvent.type(within(dialog).getByLabelText(/User name/),"alice");
        await userEvent.type(within(dialog).getByLabelText(/Password/),"secret");
        await userEvent.click(within(dialog).getByRole("button",{ name: "Log in" }));

        expect(await screen.findByText("loaded")).toBeTruthy();
        expect(rig.send).toHaveBeenCalledTimes(2);

        for (const call of rig.send.mock.calls)
        {
            const headers = new Headers((call[1] as RequestInit).headers);

            expect(headers.has("Authorization")).toBe(false);
            expect(headers.get("X-XSRF-TOKEN")).toBe("csrf-token");
        }

        await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    });

    it("retries without a dialog when another tab already logged in again",async () =>
    {
        const rig = startRig([401,200]);

        await userEvent.click(await screen.findByRole("button",{ name: "Load" }));

        expect(await screen.findByText("loaded")).toBeTruthy();
        expect(rig.send).toHaveBeenCalledTimes(2);
        expect(screen.queryByRole("dialog")).toBeNull();
    });

    it("keeps the dialog open after a wrong password",async () =>
    {
        const rig = startRig([401,200]);

        await loadWithExpiredSession(rig);

        const dialog = await screen.findByRole("dialog");

        await userEvent.type(within(dialog).getByLabelText(/User name/),"alice");
        await userEvent.type(within(dialog).getByLabelText(/Password/),"wrong");
        await userEvent.click(within(dialog).getByRole("button",{ name: "Log in" }));

        expect((await within(dialog).findByRole("alert")).textContent).toBe("Login failed");
        expect(rig.send).toHaveBeenCalledTimes(1);
    });

    it("fails the request and returns to the login view when the dialog is cancelled",async () =>
    {
        const rig = startRig([401]);

        await loadWithExpiredSession(rig);
        await userEvent.click(await screen.findByRole("button",{ name: "Cancel login" }));

        expect(await screen.findByRole("heading",{ name: "Log in" })).toBeTruthy();
        expect(rig.send).toHaveBeenCalledTimes(1);
    });

    it("uses the current path as returnTo for Okta",async () =>
    {
        window.history.pushState({},"","/users?x=1");

        const rig = startRig([401]);

        await loadWithExpiredSession(rig);

        const dialog = await screen.findByRole("dialog");

        await userEvent.click(await within(dialog).findByRole("button",{ name: "Log in with Okta" }));

        await waitFor(() => expect(rig.navigator.assigned).toEqual(["https://okta.example/authorize"]));
        expect(rig.frontEnd.callsFor(KEYS.start)[0].payload).toMatchObject({ returnTo: "/users?x=1" });
    });
});

//////////////////////////////////////////////////////////////////////////////
