//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }       from "vitest";
import { DEFAULT_ENV_PREFIX, envFileName, isValidEnvPrefix,
         toEnvFile }                  from "granite.web.client/Clients";

describe("EnvFile", () =>
{
    it("writesTheExactContentWithTheDefaultPrefix", () =>
    {
        expect(DEFAULT_ENV_PREFIX).toBe("granite");
        expect(toEnvFile("Test-Client","gc_abc","s3cret"))
            .toBe("granite-client-name=Test-Client\ngranite-client-id=gc_abc\ngranite-client-secret=s3cret\n");
    });

    it("usesTheApplicationPrefix", () =>
    {
        expect(toEnvFile("Orders","pc_abc","s3cret","poplar"))
            .toBe("poplar-client-name=Orders\npoplar-client-id=pc_abc\npoplar-client-secret=s3cret\n");
    });

    it("keepsAClientNameOnOneLine", () =>
    {
        expect(toEnvFile("A\r\nB","gc_abc","s3cret")).toContain("granite-client-name=A B\n");
    });

    it("refusesAnInvalidPrefix", () =>
    {
        expect(isValidEnvPrefix("poplar-v2.app_1")).toBe(true);
        expect(isValidEnvPrefix("")).toBe(false);
        expect(isValidEnvPrefix("bad prefix")).toBe(false);
        expect(isValidEnvPrefix("a=b")).toBe(false);
        expect(() => toEnvFile("A","gc_abc","s3cret","bad prefix")).toThrow();
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
