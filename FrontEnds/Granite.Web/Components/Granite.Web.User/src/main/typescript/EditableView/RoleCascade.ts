//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { RoleData } from "granite.frontendcontroller.service/RequestReply";

export interface IRoleOption
{
    value: string;
    label: string;
}

function impliedOf(role: string, catalog: Array<RoleData>): Array<string>
{
    return catalog.find(r => r.name === role)?.impliedRoles ?? [];
}

function transitiveImplied(role: string, catalog: Array<RoleData>): Array<string>
{
    const seen:  Array<string> = [];
    const queue: Array<string> = [...impliedOf(role,catalog)];

    while (queue.length > 0)
    {
        const next: string = queue.shift() as string;

        if (!seen.includes(next))
        {
            seen.push(next);
            queue.push(...impliedOf(next,catalog));
        }
    }

    return seen;
}

export function selectRole(selected: Array<string>, role: string, catalog: Array<RoleData>): Array<string>
{
    const result: Array<string> = [...selected];

    for (const name of [role, ...transitiveImplied(role,catalog)])
    {
        if (!result.includes(name))
        {
            result.push(name);
        }
    }

    return result;
}

export function deselectRole(selected: Array<string>, role: string, catalog: Array<RoleData>): Array<string>
{
    const removed: Array<string> = [role, ...catalog.filter(r => transitiveImplied(r.name,catalog).includes(role)).map(r => r.name)];

    return selected.filter(name => !removed.includes(name));
}

export function applySelection(previous: Array<string>, next: Array<string>, catalog: Array<RoleData> | undefined): Array<string>
{
    if (catalog == null || catalog.length === 0)
    {
        return next;
    }

    let result: Array<string> = previous;

    for (const role of previous.filter(r => !next.includes(r)))
    {
        result = deselectRole(result,role,catalog);
    }

    for (const role of next.filter(r => !previous.includes(r)))
    {
        result = selectRole(result,role,catalog);
    }

    return result;
}

export function roleOptions(catalog: Array<RoleData> | undefined, current: Array<string>): Array<IRoleOption>
{
    const options: Array<IRoleOption> = (catalog ?? []).map(r => ({ value: r.name, label: r.label }));

    for (const name of current)
    {
        if (!options.some(o => o.value === name))
        {
            options.push({ value: name, label: name });
        }
    }

    return options;
}

//////////////////////////////////////////////////////////////////////////////
