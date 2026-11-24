//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                          from "vitest";
import type { RoleData }                                 from "granite.frontendcontroller.service/RequestReply";
import { clientAuthorityOptions, clientRoles }           from "granite.web.client/EditableView";

const catalog: Array<RoleData> =
[
    { name: "ROLE_ADMIN",   label: "Admin",   impliedRoles: [] },
    { name: "ROLE_AUDITOR", label: "Auditor", impliedRoles: ["ROLE_USER"] },
    { name: "ROLE_USER",    label: "User",    impliedRoles: [] },
];

describe("ClientsAuthorityOptions", () =>
{
    it("neverOffersRoleAdmin", () =>
    {
        const options = clientAuthorityOptions(catalog,[]);

        expect(options.map(o => o.value)).toEqual(["ROLE_AUDITOR","ROLE_USER"]);
    });

    it("removesRoleAdminFromTheCatalogAndFromImpliedRoles", () =>
    {
        const withImplied: Array<RoleData> = [{ name: "ROLE_ADMIN", label: "Admin", impliedRoles: [] },
                                              { name: "ROLE_X", label: "X", impliedRoles: ["ROLE_ADMIN","ROLE_USER"] },
                                              { name: "ROLE_USER", label: "User", impliedRoles: [] }];

        expect(clientRoles(withImplied)).toEqual([{ name: "ROLE_X", label: "X", impliedRoles: ["ROLE_USER"] },
                                                  { name: "ROLE_USER", label: "User", impliedRoles: [] }]);
    });

    it("keepsLabelsAndCurrentUnknownRoles", () =>
    {
        const options = clientAuthorityOptions(catalog,["ROLE_OLD"]);

        expect(options).toContainEqual({ value: "ROLE_AUDITOR", label: "Auditor" });
        expect(options).toContainEqual({ value: "ROLE_OLD", label: "ROLE_OLD" });
    });

    it("handlesAnUndefinedCatalog", () =>
    {
        expect(clientRoles(undefined)).toEqual([]);
    });
});

//////////////////////////////////////////////////////////////////////////////
