//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

export const DEFAULT_ENV_PREFIX: string = "granite";

const ENV_PREFIX_PATTERN: RegExp = /^[A-Za-z0-9._-]+$/;

export function isValidEnvPrefix(prefix: string): boolean
{
    return ENV_PREFIX_PATTERN.test(prefix);
}

export function toEnvFile(clientName: string,clientId: string,secret: string,prefix: string = DEFAULT_ENV_PREFIX): string
{
    if (!isValidEnvPrefix(prefix))
        throw new Error("envPrefix must contain only letters, digits, '.', '_' or '-'");

    return prefix + "-client-name=" + clientName.replace(/[\r\n]+/g," ") + "\n"
         + prefix + "-client-id=" + clientId + "\n"
         + prefix + "-client-secret=" + secret + "\n";
}

export function envFileName(name: string): string
{
    return name.replace(/[^A-Za-z0-9._-]/g,"_") + ".env";
}

//////////////////////////////////////////////////////////////////////////////
