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

function renderMenu(status: AuthenticationStatus,props: IUserMenuProps,logout: () => Promise<boolean> = () => Promise.resolve(true),path: string = "/home",flags: { passwordEnabled?: boolean; passwordSet?: boolean } = {}): void
{
    const context =
    {
        state:  { status,userName: status === "anonymous" || status === "restoring" ? undefined : "alice",authorities: [],...flags },
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
    it("anonymousShowsLogin",async () =>
    {
        renderMenu("anonymous",{});

        expect(screen.getByText("Login")).toBeTruthy();
        await userEvent.click(screen.getByLabelText("User menu"));
        expect(await screen.findByRole("menuitem",{ name: "Login" })).toBeTruthy();
        expect(screen.queryByRole("menuitem",{ name: "Logout" })).toBeNull();
    });

    it("authenticatedShowsNameAndLogout",async () =>
    {
        renderMenu("authenticated",{});

        expect(screen.getByText("alice")).toBeTruthy();
        await userEvent.click(screen.getByLabelText("User menu"));
        expect(await screen.findByRole("menuitem",{ name: "Logout" })).toBeTruthy();
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
        await screen.findByRole("menuitem",{ name: "Logout" });
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

    it("logoutFirstThenPreferencesThenItems",async () =>
    {
        renderMenu("authenticated",{ preferencesPath: "/prefs",items: [{ label: "About",onClick: vi.fn() }] });

        await userEvent.click(screen.getByLabelText("User menu"));
        await screen.findByRole("menuitem",{ name: "Logout" });

        expect(screen.getAllByRole("menuitem").map((item: HTMLElement) => item.textContent)).toEqual(["Logout","Preferences","About"]);
    });

    it("loginFirstWhenAnonymous",async () =>
    {
        renderMenu("anonymous",{ preferencesPath: "/prefs" });

        await userEvent.click(screen.getByLabelText("User menu"));
        await screen.findByRole("menuitem",{ name: "Login" });

        expect(screen.getAllByRole("menuitem").map((item: HTMLElement) => item.textContent)).toEqual(["Login","Preferences"]);
    });

    it("logoutNavigatesToLogin",async () =>
    {
        const logout = vi.fn().mockResolvedValue(true);

        renderMenu("authenticated",{},logout);

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "Logout" }));

        expect(await screen.findByText("login page")).toBeTruthy();
        expect(logout).toHaveBeenCalledTimes(1);
    });

    it("restoringShowsNoLogin",async () =>
    {
        renderMenu("restoring",{});

        expect(screen.queryByText("Login")).toBeNull();
        await userEvent.click(screen.getByLabelText("User menu"));
        expect(screen.queryByRole("menuitem",{ name: "Login" })).toBeNull();
        expect(screen.queryByRole("menuitem",{ name: "Logout" })).toBeNull();
    });

    it("logoutFailureDoesNotNavigate",async () =>
    {
        const logout = vi.fn().mockResolvedValue(false);

        renderMenu("authenticated",{},logout,"/home");

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "Logout" }));

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

    it.each([
        [true,true,true],
        [false,true,false],
        [true,false,false],
    ])("changePasswordItemWhenEnabled=%s set=%s shown=%s",async (passwordEnabled: boolean,passwordSet: boolean,shown: boolean) =>
    {
        renderMenu("authenticated",{},undefined,"/home",{ passwordEnabled,passwordSet });

        await userEvent.click(screen.getByLabelText("User menu"));
        await screen.findByRole("menuitem",{ name: "Logout" });

        expect(screen.queryByRole("menuitem",{ name: "Change Password" }) !== null).toBe(shown);
    });

    it("changePasswordItemOpensDialog",async () =>
    {
        renderMenu("authenticated",{},undefined,"/home",{ passwordEnabled: true,passwordSet: true });

        await userEvent.click(screen.getByLabelText("User menu"));
        await userEvent.click(await screen.findByRole("menuitem",{ name: "Change Password" }));

        expect(await screen.findByLabelText("Current password")).toBeTruthy();
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
