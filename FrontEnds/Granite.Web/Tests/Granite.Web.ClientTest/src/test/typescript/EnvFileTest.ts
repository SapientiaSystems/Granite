//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }       from "vitest";
import { envFileName, toEnvFile }     from "granite.web.client/Clients";

describe("EnvFile", () =>
{
    it("writesTheExactContent", () =>
    {
        expect(toEnvFile("gc_abc","s3cret")).toBe("GRANITE_CLIENT_ID=gc_abc\nGRANITE_CLIENT_SECRET=s3cret\n");
    });

    it("replacesUnsafeCharactersInTheFileName", () =>
    {
        expect(envFileName("Partner A/B")).toBe("Partner_A_B.env");
    });

    it("keepsSafeCharactersInTheFileName", () =>
    {
        expect(envFileName("pa-rt.ner_1")).toBe("pa-rt.ner_1.env");
    });
});

//////////////////////////////////////////////////////////////////////////////
