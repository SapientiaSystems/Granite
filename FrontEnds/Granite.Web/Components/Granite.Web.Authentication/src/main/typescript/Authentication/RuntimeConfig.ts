//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export interface IRuntimeConfig
{
    apiBaseUrl: string;
}

declare global
{
    interface Window
    {
        __GRANITE_CONFIG__?: Partial<IRuntimeConfig>;
    }
}

export function getRuntimeConfig(): IRuntimeConfig
{
    const config: Partial<IRuntimeConfig> = window.__GRANITE_CONFIG__ ?? {};

    return {
        apiBaseUrl: (config.apiBaseUrl ?? "").replace(/\/+$/,""),
    };
}

//////////////////////////////////////////////////////////////////////////////
