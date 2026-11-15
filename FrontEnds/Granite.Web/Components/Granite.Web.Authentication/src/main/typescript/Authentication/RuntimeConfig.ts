//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export interface IRuntimeConfig
{
    apiBaseUrl:   string;
    cookiePrefix: string;
    application:  string;
}

declare global
{
    interface Window
    {
        __GRANITE_CONFIG__?: Partial<IRuntimeConfig>;
    }
}

export const DEFAULT_COOKIE_PREFIX: string = "GRANITE";

export const DEFAULT_APPLICATION: string = "granite";

const COOKIE_PREFIX_PATTERN: RegExp = /^[A-Z0-9_]+$/;

const warnedPrefixes: Set<string> = new Set<string>();

export function normalizeCookiePrefix(prefix: string | undefined): string
{
    const trimmed: string = (prefix ?? "").trim();

    if (COOKIE_PREFIX_PATTERN.test(trimmed))
        return trimmed;

    if (trimmed !== "" && !warnedPrefixes.has(trimmed))
    {
        warnedPrefixes.add(trimmed);
        console.warn("cookiePrefix \"" + trimmed + "\" is not valid (A-Z, 0-9, _); using " + DEFAULT_COOKIE_PREFIX);
    }

    return DEFAULT_COOKIE_PREFIX;
}

export function normalizeApplication(application: string | undefined): string
{
    const trimmed: string = (application ?? "").trim();

    return trimmed === "" ? DEFAULT_APPLICATION : trimmed;
}

export function getRuntimeConfig(): IRuntimeConfig
{
    const config: Partial<IRuntimeConfig> = window.__GRANITE_CONFIG__ ?? {};

    return {
        apiBaseUrl:   (config.apiBaseUrl ?? "").replace(/\/+$/,""),
        cookiePrefix: normalizeCookiePrefix(config.cookiePrefix),
        application:  normalizeApplication(config.application),
    };
}

export const APPLICATION: string = getRuntimeConfig().application;

//////////////////////////////////////////////////////////////////////////////
