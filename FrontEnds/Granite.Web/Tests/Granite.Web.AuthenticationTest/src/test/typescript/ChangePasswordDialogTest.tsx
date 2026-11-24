//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi }       from "vitest";
import { render, screen }                 from "@testing-library/react";
import userEvent                          from "@testing-library/user-event";
import { MantineProvider }                from "@mantine/core";
import { AuthenticationContext }          from "granite.web.authentication/Authentication";
import type { IAuthenticationContext,
              IChangePasswordResult }     from "granite.web.authentication/Authentication";
import { ChangePasswordDialog }           from "granite.web.authentication/Login";

function renderDialog(changePassword: (current: string,next: string) => Promise<IChangePasswordResult>,onClose: () => void = vi.fn()): void
{
    const context = { state: { status: "authenticated",userName: "alice",authorities: [] },changePassword } as unknown as IAuthenticationContext;

    render(
        <MantineProvider>
            <AuthenticationContext.Provider value={context}>
                <ChangePasswordDialog opened onClose={onClose} />
            </AuthenticationContext.Provider>
        </MantineProvider>);
}

async function fill(current: string,next: string,confirm: string): Promise<void>
{
    await userEvent.type(await screen.findByLabelText("Current password"),current);
    await userEvent.type(screen.getByLabelText("New password"),next);
    await userEvent.type(screen.getByLabelText("Confirm new password"),confirm);
}

describe("ChangePasswordDialog",() =>
{
    it("submitDisabledUntilAllFieldsFilled",async () =>
    {
        renderDialog(vi.fn());

        const button = await screen.findByRole("button",{ name: "Change Password" });

        expect((button as HTMLButtonElement).disabled).toBe(true);
        await fill("old","new-password","new-password");
        expect((button as HTMLButtonElement).disabled).toBe(false);
    });

    it("mismatchShowsErrorAndDoesNotCallService",async () =>
    {
        const changePassword = vi.fn().mockResolvedValue({ success: true });

        renderDialog(changePassword);
        await fill("old","new-password","different");
        await userEvent.click(screen.getByRole("button",{ name: "Change Password" }));

        expect(await screen.findByText("Passwords do not match")).toBeTruthy();
        expect(changePassword).not.toHaveBeenCalled();
    });

    it("serverFailureMessageIsShown",async () =>
    {
        const changePassword = vi.fn().mockResolvedValue({ success: false,message: "invalid credentials" });

        renderDialog(changePassword);
        await fill("old","new-password","new-password");
        await userEvent.click(screen.getByRole("button",{ name: "Change Password" }));

        expect(await screen.findByText("invalid credentials")).toBeTruthy();
        expect(changePassword).toHaveBeenCalledWith("old","new-password");
    });

    it("successShowsNoticeAndClose",async () =>
    {
        const onClose        = vi.fn();
        const changePassword = vi.fn().mockResolvedValue({ success: true });

        renderDialog(changePassword,onClose);
        await fill("old","new-password","new-password");
        await userEvent.click(screen.getByRole("button",{ name: "Change Password" }));

        expect(await screen.findByText("Your password has been changed. Other sessions were signed out.")).toBeTruthy();
        await userEvent.click(screen.getByRole("button",{ name: "Close" }));
        expect(onClose).toHaveBeenCalledTimes(1);
    });
});

//////////////////////////////////////////////////////////////////////////////
