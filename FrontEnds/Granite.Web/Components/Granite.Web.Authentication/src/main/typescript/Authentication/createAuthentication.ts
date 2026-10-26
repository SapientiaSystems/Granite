//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { GraniteFrontEndControllerServiceClient }       from "granite.frontendcontroller.client/RequestReply";
import { CsrfFetch }                              from "../Http/CsrfFetch";
import type { FetchFunction }                     from "../Http/CsrfFetch";
import { SessionFetch }                           from "../Http/SessionFetch";
import { AuthenticationManager }                  from "./AuthenticationManager";
import type { INavigator }                        from "./AuthenticationManager";
import { FrontEndSessionService }                 from "./FrontEndSessionService";
import type { ISessionService }                   from "./ISessionService";
import { getRuntimeConfig }                       from "./RuntimeConfig";

export const SESSION_SERVICE: string = "frontendcontroller-user-service";

export interface IAuthentication
{
    manager:               AuthenticationManager;
    createFrontEndClient:  <C>(create: (serviceUrl: string,fetchFunction: FetchFunction) => C,service: string) => C;
}

export interface IAuthenticationOptions
{
    apiBaseUrl?:    string;
    fetchFunction?: FetchFunction;
    cookieReader?:  (name: string) => string | undefined;
    navigator?:     INavigator;
    service?:       ISessionService;
}

export function createAuthentication(options: IAuthenticationOptions = {}): IAuthentication
{
    const baseUrl: string         = options.apiBaseUrl ?? getRuntimeConfig().apiBaseUrl;
    const csrf: CsrfFetch         = new CsrfFetch(options.fetchFunction,options.cookieReader);
    const session: SessionFetch   = new SessionFetch(csrf.fetch);

    const clientOver = <C>(fetchFunction: FetchFunction,create: (serviceUrl: string,fetchFunction: FetchFunction) => C,service: string): C =>
        create(baseUrl + "/" + service,fetchFunction);

    const manager: AuthenticationManager =
        new AuthenticationManager(
            options.service ??
                new FrontEndSessionService(
                    () => clientOver(csrf.fetch,(url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),SESSION_SERVICE)),
            options.navigator);

    session.setUnauthorizedHandler(() => manager.handleUnauthorized());

    return { manager, createFrontEndClient: <C>(create: (serviceUrl: string,fetchFunction: FetchFunction) => C,service: string): C => clientOver(session.fetch,create,service) };
}

//////////////////////////////////////////////////////////////////////////////
