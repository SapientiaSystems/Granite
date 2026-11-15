//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                      from "vitest";
import type { ClientData }                           from "granite.client.service/RequestReply";
import { computeChanges, fromSecretExpiry, gridCopies, pendingEditKeys, toRows, toSecretExpiry,
         validateRows }                              from "granite.web.client/EditableView";
import type { IClientRow }                           from "granite.web.client/EditableView";

function client(clientId: string, name: string, extra: Partial<ClientData> = {}): ClientData
{
    return { clientId, name, description: "", authorities: ["ROLE_USER"], owners: [], enabled: true, version: 1, ...extra };
}

function newRow(n: number, name: string, extra: Partial<IClientRow> = {}): IClientRow
{
    return { clientId: "", name, description: "", authorities: [], owners: [], enabled: true, secretExpiryDate: null,
             rowKey: `new-${n}`, isNew: true, isDetailRow: false, ...extra };
}

describe("ClientsChangeSet", () =>
{
    it("toRowsUsesTheClientIdAsKey", () =>
    {
        const rows = toRows([client("gc_a","A",{ secretExpiresAt: "2027-01-01T00:00:00Z" })]);

        expect(rows[0].rowKey).toBe("gc_a");
        expect(rows[0].isNew).toBe(false);
        expect(rows[0].secretExpiryDate).toBe("2026-12-31");
    });

    it("detectsNoChanges", () =>
    {
        const rows = toRows([client("gc_a","A")]);

        expect(computeChanges(rows,rows)).toEqual({ clientsToCreate: [], clientsToUpdate: [] });
    });

    it("detectsACreate", () =>
    {
        const changes = computeChanges([],[newRow(1," Partner ",{ authorities: ["ROLE_USER"], owners: [7], secretExpiryDate: "2026-12-31" })]);

        expect(changes.clientsToUpdate).toEqual([]);
        expect(changes.clientsToCreate).toEqual([{ name: "Partner", description: "", authorities: ["ROLE_USER"], owners: [7],
                                                   enabled: true, secretExpiresAt: "2027-01-01T00:00:00Z" }]);
    });

    it("detectsAnUpdate", () =>
    {
        const baseline = toRows([client("gc_a","A")]);
        const rows     = baseline.map(r => ({ ...r, description: "changed", authorities: ["ROLE_USER","ROLE_AUDITOR"] }));
        const changes  = computeChanges(baseline,rows);

        expect(changes.clientsToCreate).toEqual([]);
        expect(changes.clientsToUpdate).toEqual([{ clientId: "gc_a", name: "A", description: "changed",
                                                   authorities: ["ROLE_USER","ROLE_AUDITOR"], owners: [], enabled: true,
                                                   secretExpiresAt: undefined, version: 1 }]);
    });

    it("detectsAnEnabledAndExpiryChange", () =>
    {
        const baseline = toRows([client("gc_a","A")]);
        const rows     = baseline.map(r => ({ ...r, enabled: false, secretExpiryDate: "2026-12-31" }));
        const update   = computeChanges(baseline,rows).clientsToUpdate[0];

        expect(update.enabled).toBe(false);
        expect(update.secretExpiresAt).toBe("2027-01-01T00:00:00Z");
    });

    it("ignoresAuthorityOrderAndOwnerOrder", () =>
    {
        const baseline = toRows([client("gc_a","A",{ authorities: ["A","B"], owners: [1,2] })]);
        const rows     = baseline.map(r => ({ ...r, authorities: ["B","A"], owners: [2,1] }));

        expect(computeChanges(baseline,rows).clientsToUpdate).toEqual([]);
    });

    it("validateRowsRequiresAName", () =>
    {
        const problems = validateRows([newRow(1,"  ")]);

        expect(problems.get("new-1")).toContain("Name is required");
    });

    it("validateRowsRejectsNamesOver100Characters", () =>
    {
        expect(validateRows([newRow(1,"x".repeat(101))]).get("new-1")).toContain("Name must be at most 100 characters");
        expect(validateRows([newRow(1,"x".repeat(100))]).size).toBe(0);
    });

    it("validateRowsRejectsDuplicateNames", () =>
    {
        const rows     = [...toRows([client("gc_a","Alpha")]), newRow(1," alpha ")];
        const problems = validateRows(rows);

        expect(problems.get("gc_a")).toContain("Name must be unique");
        expect(problems.get("new-1")).toContain("Name must be unique");
    });

    it("validateRowsRejectsAnInvalidExpiryDate", () =>
    {
        expect(validateRows([newRow(1,"A",{ secretExpiryDate: "2026-13-40" })]).get("new-1")).toContain("Secret expires must be a valid date");
    });

    it("toSecretExpiryIsTheStartOfTheNextDayUtc", () =>
    {
        expect(toSecretExpiry("2026-12-31")).toBe("2027-01-01T00:00:00Z");
        expect(toSecretExpiry("2028-02-28")).toBe("2028-02-29T00:00:00Z");
        expect(toSecretExpiry(null)).toBeNull();
        expect(toSecretExpiry("")).toBeNull();
    });

    it("fromSecretExpiryIsTheInverse", () =>
    {
        expect(fromSecretExpiry("2027-01-01T00:00:00Z")).toBe("2026-12-31");
        expect(fromSecretExpiry(null)).toBeNull();
        expect(fromSecretExpiry(toSecretExpiry("2030-06-15"))).toBe("2030-06-15");
    });

    it("gridCopiesKeepTheBaselineIntactWhenTheGridEditsInPlace", () =>
    {
        const rows   = toRows([client("gc_a","A")]);
        const copies = gridCopies(rows);

        copies[0].description = "edited";
        copies[0].authorities.push("ROLE_AUDITOR");

        expect(rows[0].description).toBe("");
        expect(rows[0].authorities).toEqual(["ROLE_USER"]);
        expect(computeChanges(rows,copies).clientsToUpdate).toHaveLength(1);
    });

    it("pendingEditKeysIsEmptyWhenEverythingWasSaved", () =>
    {
        const rows = toRows([client("gc_a","A"),client("gc_b","B")]);

        expect(pendingEditKeys(rows,rows)).toEqual(new Set());
    });

    it("pendingEditKeysKeepsUnsavedNewAndChangedRowsEditing", () =>
    {
        const refreshed = toRows([client("gc_a","A"),client("gc_b","B")]);
        const merged    = [refreshed[0],{ ...refreshed[1], description: "changed" },newRow(1,"C")];

        expect(pendingEditKeys(refreshed,merged)).toEqual(new Set(["new-1","gc_b"]));
    });
});

//////////////////////////////////////////////////////////////////////////////
