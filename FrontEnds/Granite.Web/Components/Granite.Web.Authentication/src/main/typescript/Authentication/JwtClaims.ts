//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export interface IJwtClaims
{
    sub?:         string;
    jti?:         string;
    exp?:         number;
    email?:       string;
    name?:        string;
    authorities?: Array<string>;
}

export function decodeJwtClaims(token: string): IJwtClaims
{
    const parts: Array<string> = token.split(".");

    if (parts.length < 2)
        return {};

    try
    {
        const base64: string = parts[1].replace(/-/g,"+").replace(/_/g,"/");
        const padded: string = base64 + "=".repeat((4 - base64.length % 4) % 4);
        const binary: string = atob(padded);
        const bytes:  Uint8Array = Uint8Array.from(binary,(c: string) => c.charCodeAt(0));

        return JSON.parse(new TextDecoder().decode(bytes)) as IJwtClaims;
    }
    catch
    {
        return {};
    }
}

export function expiresAtMillis(token: string): number | undefined
{
    const exp: number | undefined = decodeJwtClaims(token).exp;

    return exp !== undefined ? exp * 1000 : undefined;
}

//////////////////////////////////////////////////////////////////////////////
