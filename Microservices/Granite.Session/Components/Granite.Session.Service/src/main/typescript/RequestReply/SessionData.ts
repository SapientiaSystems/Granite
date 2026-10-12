export
interface SessionData
{
    sessionId?: number;
    userId?: number;
    accessToken?: string;
    created?: string;
    expires?: string;
    deviceId?: string;
    values?: Map<string,unknown>;
    authorities?: Array<string>;
}
