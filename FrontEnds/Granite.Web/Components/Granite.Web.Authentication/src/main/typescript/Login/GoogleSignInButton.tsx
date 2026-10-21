//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef } from "react";
import type { ReactElement } from "react";

interface IGoogleCredentialResponse
{
    credential: string;
}

interface IGoogleIdentity
{
    accounts:
    {
        id:
        {
            initialize(options: { client_id: string; callback: (response: IGoogleCredentialResponse) => void }): void;
            renderButton(element: HTMLElement,options: Record<string,unknown>): void;
        };
    };
}

declare global
{
    interface Window
    {
        google?: IGoogleIdentity;
    }
}

const GIS_SCRIPT_URL: string = "https://accounts.google.com/gsi/client";

let gisLoader: Promise<IGoogleIdentity> | null = null;

export function loadGoogleIdentity(): Promise<IGoogleIdentity>
{
    if (window.google)
        return Promise.resolve(window.google);

    if (!gisLoader)
    {
        gisLoader =
            new Promise<IGoogleIdentity>((resolve,reject) =>
            {
                const script: HTMLScriptElement = document.createElement("script");

                script.src     = GIS_SCRIPT_URL;
                script.async   = true;
                script.onload  = () => window.google ? resolve(window.google) : reject(new Error("Google Identity Services unavailable"));
                script.onerror = () => reject(new Error("Unable to load Google Identity Services"));
                document.head.appendChild(script);
            });
    }

    return gisLoader;
}

export interface IGoogleSignInButtonProps
{
    clientId:     string;
    onCredential: (idToken: string) => void;
    onError?:     (error: Error) => void;
}

export function GoogleSignInButton({ clientId, onCredential, onError }: IGoogleSignInButtonProps): ReactElement
{
    const container = useRef<HTMLDivElement>(null);

    useEffect(() =>
    {
        let active: boolean = true;

        loadGoogleIdentity()
            .then((google: IGoogleIdentity) =>
            {
                if (!active || !container.current)
                    return;

                google.accounts.id.initialize({
                    client_id: clientId,
                    callback:  (response: IGoogleCredentialResponse) => onCredential(response.credential),
                });
                google.accounts.id.renderButton(container.current,{ theme: "outline", size: "large", width: 320 });
            })
            .catch((error: Error) => onError?.(error));

        return () => { active = false; };
    },[clientId,onCredential,onError]);

    return <div ref={container} data-testid="google-sign-in" />;
}

//////////////////////////////////////////////////////////////////////////////
