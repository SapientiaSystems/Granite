//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }   from "vitest";
import { rotateFields }           from "granite.web.client/Clients";

describe("RotateInput", () =>
{
    it("carriesTheExistingSecretExpiry", () =>
    {
        expect(rotateFields("gc_a","2026-12-31")).toEqual({ clientIdToRotate: "gc_a", secretExpiresAt: "2027-01-01T00:00:00Z" });
    });

    it("sendsNoExpiryWhenTheClientHasNone", () =>
    {
        expect(rotateFields("gc_a",null)).toEqual({ clientIdToRotate: "gc_a", secretExpiresAt: null });
    });
});

//////////////////////////////////////////////////////////////////////////////
