//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Session
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }   from "react";
import type { SessionRowData } from "granite.frontendcontroller.service/RequestReply";

export interface ISessionsResult
{
    sessions:        Array<SessionRowData>;
    failureMessages: Array<string>;
}

export interface ISessionsGridProps
{
    readonly sessions?:              Array<SessionRowData>;
    readonly currentSessionId?:      number | null;
    readonly loading?:               boolean;
    readonly includeClosed?:         boolean;
    readonly showEnd?:               boolean;
    readonly height?:                number | "grow";
    readonly onRenderToolbar?:       (toolbar: ReactElement) => void;
    readonly onIncludeClosedChange?: (includeClosed: boolean) => void;
    readonly onRefresh?:             () => void;
    readonly onEnd?:                 (sessionIds: Array<number>) => Promise<ISessionsResult>;
}

//////////////////////////////////////////////////////////////////////////////
