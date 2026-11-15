//////////////////////////////////////////////////////////////////////////////
// Granite.Web.SessionTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }  from "vitest";
import { applicationLabel, authoritiesLabel, canEndSelection, detailRowHeight, displayValue, endableSessionIds, formatInstant,
         isCurrent, isOpen, roleLabel, toSessionRows, userLabel, valueEntries, withDetailRow } from "granite.web.session/View";
import type { SessionRowData }  from "granite.frontendcontroller.service/RequestReply";

const open:    SessionRowData = { sessionId: 11, userId: 7, userName: "admin", values: { cart: 3 } };
const other:   SessionRowData = { sessionId: 12, userId: 8, userName: "alice" };
const closed:  SessionRowData = { sessionId: 13, userId: 8, userName: "alice", closedReason: "ADMIN" };

describe("SessionRows", () =>
{
    it("keys rows by session id and adds the detail row under its parent", () =>
    {
        const rows = toSessionRows([open,other]);

        expect(rows.map(r => r.rowKey)).toEqual(["11","12"]);
        expect(rows.every(r => !r.isDetailRow)).toBe(true);

        const withDetail = withDetailRow(rows,"11");

        expect(withDetail.map(r => r.rowKey)).toEqual(["11","11","12"]);
        expect(withDetail[1].isDetailRow).toBe(true);
        expect(withDetailRow(rows,null)).toHaveLength(2);
    });

    it("treats a row without closed reason as open", () =>
    {
        expect(isOpen(open)).toBe(true);
        expect(isOpen(closed)).toBe(false);
    });

    it("recognises the current session", () =>
    {
        expect(isCurrent(open,11)).toBe(true);
        expect(isCurrent(other,11)).toBe(false);
        expect(isCurrent(open,null)).toBe(false);
    });

    it("never ends the current session and skips closed ones", () =>
    {
        expect(endableSessionIds([open,other,closed],11)).toEqual([12]);
        expect(canEndSelection([other],11)).toBe(true);
        expect(canEndSelection([other,open],11)).toBe(false);
        expect(canEndSelection([closed],11)).toBe(false);
        expect(canEndSelection([],11)).toBe(false);
    });

    it("labels users by name, falling back to the id", () =>
    {
        expect(userLabel(open)).toBe("admin");
        expect(userLabel({ sessionId: 1, userId: 5 })).toBe("#5");
        expect(userLabel({ sessionId: 1 })).toBe("");
    });

    it("lists authorities sorted and comma separated", () =>
    {
        expect(authoritiesLabel({ sessionId: 1, authorities: ["ROLE_USER","ROLE_ADMIN"] })).toBe("ROLE_ADMIN, ROLE_USER");
        expect(authoritiesLabel({ sessionId: 1 })).toBe("");
    });

    it("shows authorities by their catalog labels", () =>
    {
        const roles = [{ name: "ROLE_ADMIN", label: "Admin", impliedRoles: [] }, { name: "ROLE_USER", label: "User", impliedRoles: [] }];

        expect(roleLabel("ROLE_ADMIN",roles)).toBe("Admin");
        expect(roleLabel("ROLE_OTHER",roles)).toBe("ROLE_OTHER");
        expect(authoritiesLabel({ sessionId: 1, authorities: ["ROLE_USER","ROLE_ADMIN"] },roles)).toBe("Admin, User");
    });

    it("renders values as read-only entries", () =>
    {
        expect(valueEntries({ cart: 3, flags: { a: true }, none: null }))
            .toEqual([{ id: 0, key: "cart", value: "3" },
                      { id: 1, key: "flags", value: "{\"a\":true}" },
                      { id: 2, key: "none", value: "" }]);
        expect(valueEntries(undefined)).toEqual([]);
        expect(displayValue(undefined)).toBe("");
    });

    it("formats instants and keeps unparseable text", () =>
    {
        expect(formatInstant(null)).toBe("");
        expect(formatInstant("not a date")).toBe("not a date");
        expect(formatInstant("2026-11-01T12:00:00Z")).toBe(new Date("2026-11-01T12:00:00Z").toLocaleString());
    });

    it("bounds the detail row height", () =>
    {
        expect(detailRowHeight(0)).toBe(detailRowHeight(1));
        expect(detailRowHeight(20)).toBe(detailRowHeight(8));
    });

    it("labels the application, treating a missing one as granite", () =>
    {
        expect(applicationLabel({ ...open, application: "client" })).toBe("client");
        expect(applicationLabel(open)).toBe("granite");
        expect(applicationLabel({ ...open, application: "" })).toBe("granite");
    });
});

//////////////////////////////////////////////////////////////////////////////
