//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                               from "vitest";
import { render, screen, waitFor }                                            from "@testing-library/react";
import userEvent                                                              from "@testing-library/user-event";
import { MantineProvider }                                                    from "@mantine/core";
import { MemoryRouter, Route, Routes }                                        from "react-router-dom";
import type { ReactElement }                                                  from "react";
import { AuthenticationManager }                                              from "../../main/typescript/Authentication/AuthenticationManager";
import { AuthenticationProvider, useAuthentication }                          from "../../main/typescript/Authentication/AuthenticationContext";
import { RequireAuthentication }                                              from "../../main/typescript/Authentication/RequireAuthentication";
import { LoginView }                                                          from "../../main/typescript/Login/LoginView";
import { completed, failed, fakeApi, grant, ManualTimer, MemoryTokenStorage } from "./TestSupport";
import type { IFakeSessionApi }                                               from "./TestSupport";

const NOW_SECONDS: number = 1_800_000_000;

function WhoAmI(): ReactElement
{
    const { state } = useAuthentication();

    return <div>signed in as {state.userName}</div>;
}

function renderApp(manager: AuthenticationManager,path: string): void
{
    render(
        <MantineProvider>
            <AuthenticationProvider manager={manager}>
                <MemoryRouter initialEntries={[path]}>
                    <Routes>
                        <Route path="/login" element={<LoginView googleClientId="" />} />
                        <Route path="/users" element={<RequireAuthentication><WhoAmI /></RequireAuthentication>} />
                    </Routes>
                </MemoryRouter>
            </AuthenticationProvider>
        </MantineProvider>);
}

function manager(api: IFakeSessionApi,storage: MemoryTokenStorage = new MemoryTokenStorage()): AuthenticationManager
{
    return new AuthenticationManager(api,storage,new ManualTimer(NOW_SECONDS * 1000));
}

describe("RequireAuthentication and LoginView",() =>
{
    it("redirects an anonymous visitor to the login view",() =>
    {
        renderApp(manager(fakeApi()),"/users");

        expect(screen.getByRole("heading",{ name: "Sign in" })).toBeTruthy();
    });

    it("signs in and returns to the protected route",async () =>
    {
        const api = fakeApi();

        api.login.mockReturnValue(completed(grant(1,NOW_SECONDS + 900)));
        renderApp(manager(api),"/users");

        await userEvent.type(screen.getByLabelText(/User name/),"alice");
        await userEvent.type(screen.getByLabelText(/Password/),"secret");
        await userEvent.click(screen.getByRole("button",{ name: "Sign in" }));

        expect(await screen.findByText("signed in as alice")).toBeTruthy();
        expect(api.login).toHaveBeenCalledWith("alice","secret","device-1");
    });

    it("shows the failure message and stays on the login view",async () =>
    {
        const api = fakeApi();

        api.login.mockReturnValue(failed(new Error("account locked")));
        renderApp(manager(api),"/login");

        await userEvent.type(screen.getByLabelText(/User name/),"alice");
        await userEvent.type(screen.getByLabelText(/Password/),"secret");
        await userEvent.click(screen.getByRole("button",{ name: "Sign in" }));

        expect((await screen.findByRole("alert")).textContent).toContain("account locked");
    });

    it("restores a stored session before rendering the protected route",async () =>
    {
        const api     = fakeApi();
        const storage = new MemoryTokenStorage({ refreshToken: "refresh-0", sessionId: 2002, userName: "alice" });

        api.refresh.mockReturnValue(completed(grant(1,NOW_SECONDS + 900)));
        renderApp(manager(api,storage),"/users");

        await waitFor(() => expect(screen.getByText("signed in as alice")).toBeTruthy());
        expect(api.refresh).toHaveBeenCalledWith("refresh-0");
    });

    it("hides Google sign-in when no client id is configured",() =>
    {
        renderApp(manager(fakeApi()),"/login");

        expect(screen.queryByTestId("google-sign-in")).toBeNull();
    });
});

//////////////////////////////////////////////////////////////////////////////
