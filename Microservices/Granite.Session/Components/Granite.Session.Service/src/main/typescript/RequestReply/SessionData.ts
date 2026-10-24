import {SessionClosedReason} from "./SessionClosedReason";

export
interface SessionData
{
    sessionId?: number;
    userId?: number;
    created?: string;
    expires?: string;
    deviceId?: string;
    values?: Map<string,unknown>;
    authorities?: Array<string>;
    continuityId?: string;
    closedReason?: SessionClosedReason;
    replacedBy?: number;
    copiedFrom?: number;
}
