//////////////////////////////////////////////////////////////////////////////
// Granite.Web.UserTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }      from "vitest";
import { createEntry, entryMessages, isActionAllowed, nextMacrotask, removeOrphanEntries, toEntries,
         toValues, validateEntries } from "granite.web.user/EditableView";

describe("UsersValueEntries", () =>
{
    it("roundTripsValuesThroughEntries", () =>
    {
        const values = { a: "1", b: 2 };

        expect(toValues(toEntries(values))).toEqual(values);
    });

    it("assignsDistinctEntryIds", () =>
    {
        expect(createEntry().id).not.toBe(createEntry().id);
    });

    it("reportsBlankAndDuplicateKeys", () =>
    {
        expect(entryMessages([createEntry("a", "1"), createEntry("a ", "2")])).toEqual(["Value keys must be unique (a)"]);
        expect(entryMessages([createEntry("", "1")])).toEqual(["Value keys cannot be blank"]);
        expect(entryMessages([createEntry("a", "1"), createEntry("b", "1")])).toEqual([]);
    });

    it("removesEntriesOfRowsThatNoLongerExist", () =>
    {
        const result = removeOrphanEntries({ "user-1": [createEntry("a")], "new-1": [createEntry("")] }, ["user-1"]);

        expect(Object.keys(result)).toEqual(["user-1"]);
    });

    it("nextMacrotaskRunsAfterAlreadyQueuedTimers", async () =>
    {
        const order: Array<string> = [];

        setTimeout(() => order.push("queued"), 0);
        await nextMacrotask();
        order.push("after");

        expect(order).toEqual(["queued", "after"]);
    });

    it("blocksEveryActionWhileBusy", () =>
    {
        const state = { busy: true, isEditing: true, hasSelection: true, deleteDisabled: false };

        for (const action of ["create", "edit", "save", "undo", "delete"] as const)
        {
            expect(isActionAllowed(action, state)).toBe(false);
        }
    });

    it("allowsActionsByState", () =>
    {
        const idle = { busy: false, isEditing: false, hasSelection: false, deleteDisabled: true };

        expect(isActionAllowed("create", idle)).toBe(true);
        expect(isActionAllowed("save", idle)).toBe(false);
        expect(isActionAllowed("edit", { ...idle, hasSelection: true })).toBe(true);
        expect(isActionAllowed("save", { ...idle, isEditing: true })).toBe(true);
        expect(isActionAllowed("delete", { ...idle, hasSelection: true })).toBe(false);
        expect(isActionAllowed("delete", { ...idle, hasSelection: true, deleteDisabled: false })).toBe(true);
    });

    it("validatesOnlyUsersWithProblems", () =>
    {
        const result = validateEntries({ "user-1": [createEntry("a"), createEntry("a")], "user-2": [createEntry("a")] });

        expect([...result.keys()]).toEqual(["user-1"]);
    });
});

//////////////////////////////////////////////////////////////////////////////
