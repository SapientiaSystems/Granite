//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi }                    from "vitest";
import { render, screen }                              from "@testing-library/react";
import userEvent                                       from "@testing-library/user-event";
import { MantineProvider }                             from "@mantine/core";
import { MemoryRouter, Route, Routes }                 from "react-router-dom";
import type { ReactElement }                           from "react";
import { AuthenticationContext }                       from "granite.web.authentication/Authentication";
import type { AuthenticationStatus,
              IAuthenticationContext }                 from "granite.web.authentication/Authentication";
import { UserMenu }                                    from "granite.web.authentication/Login";
import type { IUserMenuProps }                         from "granite.web.authentication/Login";

function renderMenu(status: AuthenticationStatus,props: IUserMenuProps,logout: () => Promise<boolean> = () => Promise.resolve(true),path: string = "/home"): void
{
    const context =
    {
        state:  { status,userName: status === "anonymous" || status === "restoring" ? undefined : "alice",authorities: [] },
        logout,
    } as unknown as IAuthenticationContext;

    render(
        <MantineProvider>
            <AuthenticationContext.Provider value={context}>
                <MemoryRouter initialEntries={[path]}>
                    <Routes>
                        <Route path="*" element={<UserMenu {...props} />} />
                        <Route path="/login" element={<div>login page</div>} />
                        <Route path="/prefs" element={<div>prefs page</div>} />
                    </Routes>
                </MemoryRouter>
            </AuthenticationContext.Provider>
        </MantineProvider>);
}

describe("UserMenu",() =>
{
    it("anonymousShowsLogIn",async () =>
    {
        renderMenu("anonymous",{});

        expect(screen.getByText("Log in")).toBeTruthy();
        await userEvent.click(screen.getByLabelText("User menu"));
        expect(await screen.findByRole("menuitem",{ name: "Log in" })).toBeTruthy();
        expect(screen.queryByRole("menuitem",{ name: "Log out" })).toBeNull();
    });

    it("authenticatedShowsNameAndLogout",async () =>
    {
        renderMenu("authenticated",{});

        expect(screen.getByText("alice")).toBeTruthy();
        await userEvent.click(screen.getByLabelText("User menu"));
        expect(await screen.findByRole("menuitem",{ name: "Log out" })).toBeTruthy();
    });

    it("reauthenticatingStillShowsName",() =>
    {
        renderMenu("reauthenticating",{});

        expect(screen.getByText("alice")).toBeTruthy();
    });

    it("preferencesHiddenWithoutPath",async () =>
    {
        renderMenu("authenticated",{});

        await userEvent.click(screen.getByLabelText("User menu"));
        await screen.findByRole("menuitem",{ name: "Log out" });
        expect(screen.queryByRole("menuitem",{ name: "Preferences" })).toBeNull();
    });

    it("preferencesNavigatesToPath",async () =>
    {
        renderMenu("authenticated",{ preferencesPath: "/prefs" });

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "Preferences" }));

        expect(await screen.findByText("prefs page")).toBeTruthy();
    });

    it("extraItemsRendered",async () =>
    {
        const onClick = vi.fn();

        renderMenu("authenticated",{ items: [{ label: "About", onClick }] });

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "About" }));

        expect(onClick).toHaveBeenCalledTimes(1);
    });

    it("logoutNavigatesToLogin",async () =>
    {
        const logout = vi.fn().mockResolvedValue(true);

        renderMenu("authenticated",{},logout);

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "Log out" }));

        expect(await screen.findByText("login page")).toBeTruthy();
        expect(logout).toHaveBeenCalledTimes(1);
    });

    it("restoringShowsNoLogIn",async () =>
    {
        renderMenu("restoring",{});

        expect(screen.queryByText("Log in")).toBeNull();
        await userEvent.click(screen.getByLabelText("User menu"));
        expect(screen.queryByRole("menuitem",{ name: "Log in" })).toBeNull();
        expect(screen.queryByRole("menuitem",{ name: "Log out" })).toBeNull();
    });

    it("logoutFailureDoesNotNavigate",async () =>
    {
        const logout = vi.fn().mockResolvedValue(false);

        renderMenu("authenticated",{},logout,"/home");

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "Log out" }));

        expect(logout).toHaveBeenCalledTimes(1);
        expect(screen.queryByText("login page")).toBeNull();
        expect(screen.getByText("alice")).toBeTruthy();
    });

    it("activeMatchesPathSegments",() =>
    {
        renderMenu("authenticated",{ preferencesPath: "/user" },undefined,"/users");
        expect(screen.getByLabelText("User menu").closest(".app-bar-user-menu")?.classList.contains("active")).toBe(false);
    });

    it("activeOnExactAndNestedPath",() =>
    {
        renderMenu("authenticated",{ preferencesPath: "/user" },undefined,"/user/detail");
        expect(screen.getByLabelText("User menu").closest(".app-bar-user-menu")?.classList.contains("active")).toBe(true);
    });

    it("rendersWithoutAppHostCss",() =>
    {
        renderMenu("authenticated",{ className: "custom",style: { height: 40 } });

        const button: HTMLElement | null | undefined = screen.getByLabelText("User menu").closest(".app-bar-user-menu");

        expect(button?.classList.contains("custom")).toBe(true);
        expect(button?.style.height).toBe("40px");
        expect(screen.getByText("alice")).toBeTruthy();
        expect(button?.querySelector("svg")?.getAttribute("stroke")).toBe("currentColor");
        expect(button?.innerHTML.toLowerCase().includes("white")).toBe(false);
    });
});

//////////////////////////////////////////////////////////////////////////////
