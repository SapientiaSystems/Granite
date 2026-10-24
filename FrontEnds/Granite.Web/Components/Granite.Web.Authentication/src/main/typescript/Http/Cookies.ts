//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export function readCookie(name: string,source: string = document.cookie): string | undefined
{
    for (const part of source.split(";"))
    {
        const separator: number = part.indexOf("=");

        if (separator > 0 && part.slice(0,separator).trim() === name)
            return part.slice(separator + 1).trim();
    }

    return undefined;
}

//////////////////////////////////////////////////////////////////////////////
