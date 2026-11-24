//////////////////////////////////////////////////////////////////////////////
// Granite.Web.NavigatorTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                       from "vitest";
import { averageOf, breakdownTotal, countOf, dayTotals, formatAsOf, formatAverage, isForbidden,
         percentOf, roleLabels, segments, sparkHeights, stackedDays } from "granite.web.navigator/Home";
import type { HomeBreakdownData, HomeCountData }      from "granite.frontendcontroller.service/RequestReply";

const days: Array<HomeBreakdownData> =
[
    { key: "2026-11-01", label: "1", counts: { OKTA: 2, PASSWORD: 2 } },
    { key: "2026-11-02", label: "2", counts: { OKTA: 1, PASSWORD: 0 } },
];

describe("HomeDashboard",() =>
{
    it("prefers the catalog role labels and falls back to the authorities",() =>
    {
        expect(roleLabels(["Admin"],["ROLE_ADMIN"])).toEqual(["Admin"]);
        expect(roleLabels([],["ROLE_ADMIN","ROLE_PRICE_REVIEWER","SCOPE_internal"])).toEqual(["Admin","Price Reviewer"]);
        expect(roleLabels(null,[])).toEqual([]);
    });

    it("splits counts into percentages",() =>
    {
        const items: Array<HomeCountData> = [{ key: "A", label: "A", count: 3 }, { key: "B", label: "B", count: 1 }];

        expect(segments(items).map(s => s.pct)).toEqual([75,25]);
        expect(segments([{ key: "A", label: "A", count: 0 }])[0].pct).toBe(0);
        expect(segments(null)).toEqual([]);
        expect(countOf(items,"B")).toBe(1);
        expect(countOf(items,"C")).toBe(0);
    });

    it("scales bars and sparklines to the largest value",() =>
    {
        expect(percentOf(5,10)).toBe(50);
        expect(percentOf(3,0)).toBe(0);
        expect(percentOf(12,10)).toBe(100);
        expect(sparkHeights([{ key: "a", label: "", count: 4 }, { key: "b", label: "", count: 1 }, { key: "c", label: "", count: 0 }],40)).toEqual([40,10,0]);
        expect(sparkHeights([{ key: "a", label: "", count: 0 }],40)).toEqual([0]);
    });

    it("totals and stacks daily logins",() =>
    {
        expect(dayTotals(days).map(d => d.count)).toEqual([4,1]);

        const stacked = stackedDays(days,["OKTA","PASSWORD","ACCESS_TOKEN"],100);

        expect(stacked[0].segments.map(s => s.height)).toEqual([50,50,0]);
        expect(stacked[1].segments.map(s => s.height)).toEqual([25,0,0]);
        expect(stacked[1].total).toBe(1);
        expect(stackedDays(null,["OKTA"],100)).toEqual([]);
        expect(breakdownTotal(days[0])).toBe(4);
    });

    it("formats averages and the generated time",() =>
    {
        expect(formatAverage(4.25)).toBe("4.3");
        expect(formatAverage(null)).toBe("–");
        expect(averageOf([{ key: "needs", label: "", average: 3.8 }],"needs")).toBe(3.8);
        expect(averageOf([],"needs")).toBeNull();
        expect(formatAsOf(null)).toBe("");
        expect(formatAsOf("not a date")).toBe("");
        expect(formatAsOf("2026-11-02T18:00:00Z")).not.toBe("");
    });

    it("recognises a forbidden reply",() =>
    {
        expect(isForbidden({ getStatus: () => 403 })).toBe(true);
        expect(isForbidden({ getStatus: () => 500 })).toBe(false);
        expect(isForbidden(new Error("boom"))).toBe(false);
        expect(isForbidden(null)).toBe(false);
    });
});

//////////////////////////////////////////////////////////////////////////////
