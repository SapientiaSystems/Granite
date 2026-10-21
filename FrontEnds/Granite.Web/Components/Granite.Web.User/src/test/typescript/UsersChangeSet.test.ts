//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it } from "vitest";
import type { UserData } from "granite.user.service/RequestReply";
import { computeChanges, displayValue, findDuplicateKeys, mergePending, normalizeAuthorities,
         toRows, validateRows }                   from "../../main/typescript/EditableView/UsersChangeSet";
import type { IUserRow }                          from "../../main/typescript/EditableView/UsersChangeSet";

function user(userId: number, userName: string, authorities: Array<string>, values?: Record<string, unknown>): UserData
{
    return { userId, userName, password: "********", authorities, values };
}

function newRow(n: number, userName: string, password: string, authorities: Array<string> = [],
                values: Record<string, unknown> = {}): IUserRow
{
    return { userId: 0, userName, password, authorities, values, rowKey: `new-${n}`, isNew: true, isDetailRow: false };
}

describe("UsersChangeSet", () =>
{
    it("normalizesAuthorities", () =>
    {
        expect(normalizeAuthorities([" b ", "a", "", "  ", "b", "c", "a"])).toEqual(["b", "a", "c"]);
    });

    it("nullValuesDisplayAsEmptyString", () =>
    {
        expect(displayValue(null)).toBe("");
        expect(displayValue(undefined)).toBe("");
        expect(displayValue(5)).toBe("5");
        expect(displayValue(false)).toBe("false");
    });

    it("toRowsBuildsKeys", () =>
    {
        const rows = toRows([user(7, "ann", ["ADMIN"])]);

        expect(rows[0].rowKey).toBe("user-7");
        expect(rows[0].isNew).toBe(false);
        expect(rows[0].isDetailRow).toBe(false);
    });

    it("validateFlagsBlankNewUserFields", () =>
    {
        const result = validateRows([newRow(1, " ", ""), newRow(2, "bob", "pw"), ...toRows([user(1, "", [])])]);

        expect(result.get("new-1")?.length).toBe(2);
        expect(result.has("new-2")).toBe(false);
        expect(result.has("user-1")).toBe(false);
    });

    it("validateFlagsDuplicateAndBlankKeys", () =>
    {
        const result = validateRows([newRow(1, "bob", "pw", [], { a: 1, " a ": 2, " ": 3 }),
                                     ...toRows([user(1, "ann", [], { x: 1, "": 2 })])]);

        expect(result.get("new-1")?.length).toBe(2);
        expect(result.get("user-1")?.length).toBe(1);
    });

    it("newRowsBecomeCreates", () =>
    {
        const { usersToCreate, usersToUpdate } = computeChanges([], [newRow(1, "bob", "pw", [" ADMIN ", "ADMIN"], { k: "v" })]);

        expect(usersToUpdate).toEqual([]);
        expect(usersToCreate).toEqual([{ userName: "bob", password: "pw", authorities: ["ADMIN"], values: { k: "v" } }]);
    });

    it("changedAuthoritiesOrValuesBecomeUpdates", () =>
    {
        const baseline = toRows([user(1, "ann", ["A"], { k: "1" }), user(2, "bob", ["B"], { k: "1" })]);
        const rows     = [{ ...baseline[0], authorities: ["A", "C"] }, { ...baseline[1], values: { k: "2" } }];
        const result   = computeChanges(baseline, rows);

        expect(result.usersToCreate).toEqual([]);
        expect(result.usersToUpdate).toEqual([{ userId: 1, authorities: ["A", "C"], values: { k: "1" } },
                                              { userId: 2, authorities: ["B"], values: { k: "2" } }]);
    });

    it("unchangedRowsAreNotSent", () =>
    {
        const baseline = toRows([user(1, "ann", ["A"], { k: 1 })]);
        const rows     = [{ ...baseline[0], values: { k: "1" } }];

        expect(computeChanges(baseline, rows)).toEqual({ usersToCreate: [], usersToUpdate: [] });
    });

    it("updatesNeverIncludePassword", () =>
    {
        const baseline = toRows([user(1, "ann", ["A"])]);
        const result   = computeChanges(baseline, [{ ...baseline[0], authorities: ["B"] }]);

        expect(result.usersToUpdate[0]).not.toHaveProperty("password");
        expect(result.usersToUpdate[0]).not.toHaveProperty("userName");
    });

    it("mergeKeepsUnacceptedEdits", () =>
    {
        const baseline  = toRows([user(1, "ann", ["A"])]);
        const rows      = [{ ...baseline[0], authorities: ["B"] }];
        const refreshed = [user(1, "ann", ["A"])];
        const merged    = mergePending(refreshed, rows, baseline);

        expect(merged.length).toBe(1);
        expect(merged[0].authorities).toEqual(["B"]);
    });

    it("mergeDropsAcceptedEdits", () =>
    {
        const baseline  = toRows([user(1, "ann", ["A"])]);
        const rows      = [{ ...baseline[0], authorities: [" ROLE_ADMIN", "ROLE_ADMIN"] }];
        const refreshed = [user(1, "ann", ["ROLE_ADMIN"])];
        const merged    = mergePending(refreshed, rows, baseline);

        expect(merged[0]).toEqual(toRows(refreshed)[0]);
    });

    it("mergeKeepsUnsavedNewRowsNotCreated", () =>
    {
        const baseline  = toRows([user(1, "ann", ["A"])]);
        const rows      = [...baseline, newRow(1, "bob", "pw"), newRow(2, "carol", "pw")];
        const merged    = mergePending([user(1, "ann", ["A"]), user(3, "carol", [])], rows, baseline);

        expect(merged.map(r => r.rowKey)).toEqual(["user-1", "user-3", "new-1"]);
    });

    it("findDuplicateKeysFlagsTrimmedDuplicates", () =>
    {
        const result = findDuplicateKeys([{ key: "a", value: 1 }, { key: " a ", value: 2 }, { key: "b", value: 3 }]);

        expect(result).toEqual(["a"]);
        expect(findDuplicateKeys([{ key: "a", value: 1 }, { key: "b", value: 2 }])).toEqual([]);
    });

    it("computeChangesTrimsValueKeys", () =>
    {
        const baseline = toRows([user(1, "ann", ["A"], { k: "1" })]);
        const result   = computeChanges(baseline, [{ ...baseline[0], values: { " k ": "2" } }, newRow(1, "bob", "pw", [], { " z ": "1" })]);

        expect(result.usersToUpdate[0].values).toEqual({ k: "2" });
        expect(result.usersToCreate[0].values).toEqual({ z: "1" });
    });

    it("mergeKeepsRejectedDuplicateNewRow", () =>
    {
        const baseline = toRows([user(1, "bob", ["A"])]);
        const rows     = [...baseline, newRow(1, "bob", "pw")];
        const merged   = mergePending([user(1, "bob", ["A"])], rows, baseline);

        expect(merged.map(r => r.rowKey)).toEqual(["user-1", "new-1"]);
    });

    it("mergeDropsCreatedNewRowMatchingTrimmedName", () =>
    {
        const baseline = toRows([user(1, "ann", ["A"])]);
        const rows     = [...baseline, newRow(1, "bob ", "pw")];
        const merged   = mergePending([user(1, "ann", ["A"]), user(2, "bob", [])], rows, baseline);

        expect(merged.map(r => r.rowKey)).toEqual(["user-1", "user-2"]);
    });

    it("authorityReorderIsNotAnUpdate", () =>
    {
        const baseline = toRows([user(1, "ann", ["A", "B"])]);

        expect(computeChanges(baseline, [{ ...baseline[0], authorities: ["B", "A"] }]).usersToUpdate).toEqual([]);
    });
});

//////////////////////////////////////////////////////////////////////////////
