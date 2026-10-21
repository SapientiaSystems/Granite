//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it } from "vitest";
import { createEntry, entryMessages, toEntries, toValues, validateEntries }
    from "../../main/typescript/EditableView/UsersValueEntries";

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

    it("validatesOnlyUsersWithProblems", () =>
    {
        const result = validateEntries({ "user-1": [createEntry("a"), createEntry("a")], "user-2": [createEntry("a")] });

        expect([...result.keys()]).toEqual(["user-1"]);
    });
});

//////////////////////////////////////////////////////////////////////////////
