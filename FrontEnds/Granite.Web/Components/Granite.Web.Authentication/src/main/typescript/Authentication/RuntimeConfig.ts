//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export interface IRuntimeConfig
{
    apiBaseUrl:   string;
    cookiePrefix: string;
}

declare global
{
    interface Window
    {
        __GRANITE_CONFIG__?: Partial<IRuntimeConfig>;
    }
}

export const DEFAULT_COOKIE_PREFIX: string = "GRANITE";

const COOKIE_PREFIX_PATTERN: RegExp = /^[A-Z0-9_]+$/;

export function normalizeCookiePrefix(prefix: string | undefined): string
{
    const trimmed: string = (prefix ?? "").trim();

    return COOKIE_PREFIX_PATTERN.test(trimmed) ? trimmed : DEFAULT_COOKIE_PREFIX;
}

export function getRuntimeConfig(): IRuntimeConfig
{
    const config: Partial<IRuntimeConfig> = window.__GRANITE_CONFIG__ ?? {};

    return {
        apiBaseUrl:   (config.apiBaseUrl ?? "").replace(/\/+$/,""),
        cookiePrefix: normalizeCookiePrefix(config.cookiePrefix),
    };
}

//////////////////////////////////////////////////////////////////////////////
