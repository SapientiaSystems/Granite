//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                   from "vitest";
import { canEdit, hasAuthority }                  from "../../main/typescript/Authentication/Authorities";
import { ANONYMOUS }                              from "../../main/typescript/Authentication/IAuthenticationState";
import type { IAuthenticationState }              from "../../main/typescript/Authentication/IAuthenticationState";

const AUTHENTICATED_ADMIN: IAuthenticationState = {
    status:      "authenticated",
    userName:    "alice",
    authorities: ["ROLE_User","ROLE_Admin"],
};

const AUTHENTICATED_AUDITOR: IAuthenticationState = {
    status:      "authenticated",
    userName:    "carol",
    authorities: ["ROLE_Auditor"],
};

describe("canEdit",() =>
{
    it("is true for an authenticated admin",() =>
    {
        expect(canEdit(AUTHENTICATED_ADMIN)).toBe(true);
    });

    it("is false for an authenticated auditor",() =>
    {
        expect(canEdit(AUTHENTICATED_AUDITOR)).toBe(false);
    });

    it("keeps the last known authorities while re-authenticating",() =>
    {
        expect(canEdit({ ...AUTHENTICATED_ADMIN, status: "reauthenticating" })).toBe(true);
    });

    it("isAdminRecognisesMixedCase",() =>
    {
        expect(canEdit({ ...AUTHENTICATED_ADMIN, authorities: ["ROLE_Admin"] })).toBe(true);
        expect(canEdit({ ...AUTHENTICATED_ADMIN, authorities: ["ROLE_ADMIN"] })).toBe(false);
    });

    it("is false when anonymous even if ROLE_Admin is listed",() =>
    {
        expect(canEdit({ ...ANONYMOUS, authorities: ["ROLE_Admin"] })).toBe(false);
    });
});

describe("hasAuthority",() =>
{
    it("is true for an authenticated user holding the authority",() =>
    {
        expect(hasAuthority(AUTHENTICATED_AUDITOR,"ROLE_Auditor")).toBe(true);
    });

    it("is false for an authenticated user lacking the authority",() =>
    {
        expect(hasAuthority(AUTHENTICATED_AUDITOR,"ROLE_Admin")).toBe(false);
    });

    it("is false when not authenticated even if the authority is listed",() =>
    {
        expect(hasAuthority({ ...ANONYMOUS, authorities: ["ROLE_Auditor"] },"ROLE_Auditor")).toBe(false);
    });
});

//////////////////////////////////////////////////////////////////////////////
