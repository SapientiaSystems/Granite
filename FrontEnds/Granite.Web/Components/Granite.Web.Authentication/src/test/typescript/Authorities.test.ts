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
    authorities: ["ROLE_USER","ROLE_ADMIN"],
};

const AUTHENTICATED_AUDITOR: IAuthenticationState = {
    status:      "authenticated",
    userName:    "carol",
    authorities: ["ROLE_AUDITOR"],
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

    it("is false when anonymous even if ROLE_ADMIN is listed",() =>
    {
        expect(canEdit({ ...ANONYMOUS, authorities: ["ROLE_ADMIN"] })).toBe(false);
    });
});

describe("hasAuthority",() =>
{
    it("is true for an authenticated user holding the authority",() =>
    {
        expect(hasAuthority(AUTHENTICATED_AUDITOR,"ROLE_AUDITOR")).toBe(true);
    });

    it("is false for an authenticated user lacking the authority",() =>
    {
        expect(hasAuthority(AUTHENTICATED_AUDITOR,"ROLE_ADMIN")).toBe(false);
    });

    it("is false when not authenticated even if the authority is listed",() =>
    {
        expect(hasAuthority({ ...ANONYMOUS, authorities: ["ROLE_AUDITOR"] },"ROLE_AUDITOR")).toBe(false);
    });
});

//////////////////////////////////////////////////////////////////////////////
