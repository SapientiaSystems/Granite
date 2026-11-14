//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

export
interface SessionRowData
{
    sessionId:     number;
    userId?:       number | null;
    userName?:     string | null;
    authorities?:  Array<string>;
    deviceId?:     string | null;
    loginMethod?:  string | null;
    created?:      string | null;
    lastActivity?: string | null;
    expires?:      string | null;
    closedReason?: string | null;
    continuityId?: string | null;
    application?:  string | null;
    replacedBy?:   number | null;
    copiedFrom?:   number | null;
    values?:       Record<string,unknown>;
}

//////////////////////////////////////////////////////////////////////////////
