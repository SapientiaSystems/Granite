//////////////////////////////////////////////////////////////////////////////
// Granite.Web.UserTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                   from "vitest";
import { applySelection, deselectRole, roleOptions, selectRole }  from "granite.web.user/EditableView";

const catalog: Array<{ name: string; label: string; impliedRoles: Array<string> }> =
[
    { name: "ROLE_ADMIN",                label: "Administrator",       impliedRoles: [] },
    { name: "ROLE_PROMOTIONS_REVIEWER",  label: "Promotions Reviewer", impliedRoles: [] },
    { name: "ROLE_PROMOTIONS_PLANNER",   label: "Promotions Planner",  impliedRoles: ["ROLE_PROMOTIONS_REVIEWER"] },
    { name: "ROLE_PROMOTIONS_APPROVER",  label: "Promotions Approver", impliedRoles: ["ROLE_PROMOTIONS_PLANNER"] },
];

describe("RoleCascade", () =>
{
    it("selectAddsImpliedRoles", () =>
    {
        const result: Array<string> = selectRole([],"ROLE_PROMOTIONS_APPROVER",catalog);

        expect([...result].sort()).toEqual(["ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER"]);
    });

    it("removeCascadesToDependents", () =>
    {
        const all:    Array<string> = ["ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER"];
        const result: Array<string> = deselectRole(all,"ROLE_PROMOTIONS_REVIEWER",catalog);

        expect(result).toEqual([]);
        expect(deselectRole(all,"ROLE_PROMOTIONS_PLANNER",catalog)).toEqual(["ROLE_PROMOTIONS_REVIEWER"]);
    });

    it("adminIsIndependent", () =>
    {
        expect(selectRole(["ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER"],"ROLE_ADMIN",catalog))
            .toEqual(["ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER","ROLE_ADMIN"]);
        expect(deselectRole(["ROLE_ADMIN","ROLE_PROMOTIONS_REVIEWER"],"ROLE_PROMOTIONS_REVIEWER",catalog)).toEqual(["ROLE_ADMIN"]);
        expect(deselectRole(["ROLE_ADMIN","ROLE_PROMOTIONS_REVIEWER"],"ROLE_ADMIN",catalog)).toEqual(["ROLE_PROMOTIONS_REVIEWER"]);
    });

    it("keepsNonCatalogRoles", () =>
    {
        const held: Array<string> = ["ROLE_LEGACY","ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER"];

        expect(deselectRole(held,"ROLE_PROMOTIONS_REVIEWER",catalog)).toEqual(["ROLE_LEGACY"]);
        expect(selectRole(["ROLE_LEGACY"],"ROLE_PROMOTIONS_PLANNER",catalog)).toContain("ROLE_LEGACY");
        expect(deselectRole(held,"ROLE_LEGACY",catalog)).not.toContain("ROLE_LEGACY");
        expect(roleOptions(catalog,["ROLE_LEGACY"])).toContainEqual({ value: "ROLE_LEGACY", label: "ROLE_LEGACY" });
        expect(roleOptions(catalog,[]).map(o => o.label)).toEqual(["Administrator","Promotions Reviewer","Promotions Planner","Promotions Approver"]);
    });

    it("missingCatalogFallsBack", () =>
    {
        expect(roleOptions([],["ROLE_A","ROLE_B"])).toEqual([{ value: "ROLE_A", label: "ROLE_A" },{ value: "ROLE_B", label: "ROLE_B" }]);
        expect(roleOptions(undefined,["ROLE_A"])).toEqual([{ value: "ROLE_A", label: "ROLE_A" }]);
        expect(selectRole(["ROLE_A"],"ROLE_B",[])).toEqual(["ROLE_A","ROLE_B"]);
        expect(deselectRole(["ROLE_A","ROLE_B"],"ROLE_A",[])).toEqual(["ROLE_B"]);
        expect(applySelection(["ROLE_A"],["ROLE_A","ROLE_C"],[])).toEqual(["ROLE_A","ROLE_C"]);
    });

    it("applySelectionHandlesAddAndRemove", () =>
    {
        expect([...applySelection([],["ROLE_PROMOTIONS_APPROVER"],catalog)].sort())
            .toEqual(["ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER"]);
        expect(applySelection(["ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_PLANNER","ROLE_PROMOTIONS_REVIEWER"],
                              ["ROLE_PROMOTIONS_APPROVER","ROLE_PROMOTIONS_REVIEWER"],catalog))
            .toEqual(["ROLE_PROMOTIONS_REVIEWER"]);
    });
});

//////////////////////////////////////////////////////////////////////////////
