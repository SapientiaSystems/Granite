//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Content
//////////////////////////////////////////////////////////////////////////////

export type ContentLocation =
    | { readonly view: "topics" }
    | { readonly view: "article"; readonly key: string };

export interface IContentHistory
{
    readonly entries: Array<ContentLocation>;
    readonly index:   number;
}

export const INITIAL_HISTORY: IContentHistory = { entries: [{ view: "topics" }], index: 0 };

export function currentLocation(history: IContentHistory): ContentLocation
{
    return history.entries[history.index] ?? { view: "topics" };
}

export function navigate(history: IContentHistory, location: ContentLocation): IContentHistory
{
    const current: ContentLocation = currentLocation(history);

    if (current.view === location.view && (current.view === "topics" || (location.view === "article" && current.key === location.key)))
    {
        return history;
    }

    const entries: Array<ContentLocation> = [...history.entries.slice(0,history.index + 1), location];

    return { entries, index: entries.length - 1 };
}

export function canGoBack(history: IContentHistory): boolean
{
    return history.index > 0;
}

export function canGoForward(history: IContentHistory): boolean
{
    return history.index < history.entries.length - 1;
}

export function back(history: IContentHistory): IContentHistory
{
    return canGoBack(history) ? { ...history, index: history.index - 1 } : history;
}

export function forward(history: IContentHistory): IContentHistory
{
    return canGoForward(history) ? { ...history, index: history.index + 1 } : history;
}

//////////////////////////////////////////////////////////////////////////////
