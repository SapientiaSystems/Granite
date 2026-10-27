//////////////////////////////////////////////////////////////////////////////
// Granite.Web.UserTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                  from "vitest";
import { createEntry, entryMessages, isReservedValueKey, toEntries,
         validateEntries }                                       from "granite.web.user/EditableView";

describe("UserTest", () =>
{
    it("reservedValueKeysAreReadOnly", () =>
    {
        expect(isReservedValueKey("preferences.granite.theme")).toBe(true);
        expect(isReservedValueKey("granite.rolesAssigned")).toBe(true);
        expect(isReservedValueKey("department")).toBe(false);
        expect(isReservedValueKey("myPreferences.x")).toBe(false);
    });

    it("loadedReservedEntriesAreLockedAndValid", () =>
    {
        const entries = toEntries({ "granite.rolesAssigned": true, "preferences.granite.theme": "dark", department: "a" });

        expect(entries.map(e => e.locked === true)).toEqual([true, true, false]);
        expect(entryMessages(entries)).toEqual([]);
    });

    it("rejectsAddingOrRenamingToReservedKeys", () =>
    {
        const added   = createEntry("granite.x", "1");
        const renamed = { ...toEntries({ department: "a" })[0], key: "preferences.department" };

        expect(entryMessages([added])).toEqual(["Value keys cannot start with preferences. or granite. (granite.x)"]);
        expect(entryMessages([renamed])).toEqual(["Value keys cannot start with preferences. or granite. (preferences.department)"]);
        expect([...validateEntries({ "user-1": [added] }).keys()]).toEqual(["user-1"]);
    });
});

//////////////////////////////////////////////////////////////////////////////
