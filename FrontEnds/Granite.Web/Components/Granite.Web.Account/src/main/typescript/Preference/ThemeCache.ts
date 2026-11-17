//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Account
//////////////////////////////////////////////////////////////////////////////

export type Theme = "light" | "dark";

export const THEME_KEY: string = "granite.theme";

export function isTheme(value: unknown): value is Theme
{
    return value === "light" || value === "dark";
}

export function readCachedTheme(): Theme
{
    try
    {
        const cached: string | null = window.localStorage.getItem(THEME_KEY);

        return isTheme(cached) ? cached : "dark";
    }
    catch
    {
        return "dark";
    }
}

export function writeCachedTheme(theme: Theme): void
{
    try
    {
        window.localStorage.setItem(THEME_KEY,theme);
    }
    catch
    {
    }
}

//////////////////////////////////////////////////////////////////////////////
