//////////////////////////////////////////////////////////////////////////////
// Granite.Web.UserTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                from "vitest";
import { createEntry, isReservedValueKey }     from "granite.web.user/EditableView";

describe("UserTest", () =>
{
    it("reservedValueKeysAreReadOnly", () =>
    {
        expect(isReservedValueKey("preferences.granite.theme")).toBe(true);
        expect(isReservedValueKey("granite.rolesAssigned")).toBe(true);
        expect(isReservedValueKey("department")).toBe(false);
        expect(isReservedValueKey("myPreferences.x")).toBe(false);
        expect(isReservedValueKey(createEntry("granite.x").key)).toBe(true);
    });
});

//////////////////////////////////////////////////////////////////////////////
