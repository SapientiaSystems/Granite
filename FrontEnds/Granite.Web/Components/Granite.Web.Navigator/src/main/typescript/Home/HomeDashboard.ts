//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import type { HomeAverageData, HomeBreakdownData,
              HomeCountData }                      from "granite.frontendcontroller.service/RequestReply";

export interface ISegment
{
    key:   string;
    label: string;
    count: number;
    pct:   number;
}

export interface IStackedDay
{
    key:      string;
    label:    string;
    total:    number;
    segments: Array<{ key: string; height: number }>;
}

export function roleLabels(outputRoles: Array<string> | undefined | null, authorities: Array<string>): Array<string>
{
    if (outputRoles != null && outputRoles.length > 0)
    {
        return outputRoles;
    }

    return authorities
        .filter(a => a.startsWith("ROLE_"))
        .map(a => a.substring(5).split("_").map(w => w.charAt(0) + w.substring(1).toLowerCase()).join(" "));
}

export function countOf(items: Array<HomeCountData> | undefined | null, key: string): number
{
    return items?.find(i => i.key === key)?.count ?? 0;
}

export function segments(items: Array<HomeCountData> | undefined | null): Array<ISegment>
{
    const list:  Array<HomeCountData> = items ?? [];
    const total: number               = list.reduce((sum,i) => sum + i.count,0);

    return list.map(i => ({ key: i.key, label: i.label, count: i.count, pct: total === 0 ? 0 : i.count / total * 100 }));
}

export function percentOf(count: number, max: number): number
{
    return max <= 0 ? 0 : Math.min(100,count / max * 100);
}

export function sparkHeights(items: Array<HomeCountData> | undefined | null, height: number): Array<number>
{
    const list: Array<HomeCountData> = items ?? [];
    const max:  number               = Math.max(0,...list.map(i => i.count));

    return list.map(i => max === 0 || i.count === 0 ? 0 : Math.max(2,Math.round(i.count / max * height)));
}

export function dayTotals(days: Array<HomeBreakdownData> | undefined | null): Array<HomeCountData>
{
    return (days ?? []).map(d => ({ key: d.key, label: d.label, count: Object.values(d.counts ?? {}).reduce((s,n) => s + n,0) }));
}

export function stackedDays(days: Array<HomeBreakdownData> | undefined | null, keys: Array<string>, height: number): Array<IStackedDay>
{
    const totals: Array<HomeCountData> = dayTotals(days);
    const max:    number               = Math.max(0,...totals.map(t => t.count));

    return (days ?? []).map((d,index) => (
    {
        key:      d.key,
        label:    d.label,
        total:    totals[index].count,
        segments: keys.map(k => ({ key: k, height: max === 0 ? 0 : Math.round((d.counts?.[k] ?? 0) / max * height) })),
    }));
}

export function breakdownTotal(item: HomeBreakdownData): number
{
    return Object.values(item.counts ?? {}).reduce((s,n) => s + n,0);
}

export function formatAverage(average: number | undefined | null): string
{
    return average == null ? "–" : average.toFixed(1);
}

export function averageOf(averages: Array<HomeAverageData> | undefined | null, key: string): number | null
{
    return averages?.find(a => a.key === key)?.average ?? null;
}

export function formatAsOf(generated: string | undefined | null): string
{
    if (generated == null)
    {
        return "";
    }

    const date: Date = new Date(generated);

    return isNaN(date.getTime()) ? "" : date.toLocaleTimeString("en-US",{ hour: "2-digit", minute: "2-digit" });
}

export function isForbidden(error: unknown): boolean
{
    const candidate = error as { getStatus?: () => number } | null;

    return candidate != null && typeof candidate.getStatus === "function" && candidate.getStatus() === 403;
}

//////////////////////////////////////////////////////////////////////////////
