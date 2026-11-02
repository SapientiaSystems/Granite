import {SessionClosedReason} from "./SessionClosedReason";
import {SessionLoginMethod} from "./SessionLoginMethod";

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
    lastActivity?: string;
    loginMethod?: SessionLoginMethod;
}
