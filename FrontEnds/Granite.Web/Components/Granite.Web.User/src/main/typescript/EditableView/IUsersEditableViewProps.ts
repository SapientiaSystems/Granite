//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }    from "react";
import type { UserData } from "granite.user.service/RequestReply";

export interface IUsersEditableViewProps
{
    readonly users?:     Array<UserData>;
    readonly showCreate?:      boolean;
    readonly showEdit?:        boolean;
    readonly showSave?:        boolean;
    readonly showUndo?:        boolean;
    readonly showApprove?:     boolean;
    readonly showCancel?:      boolean;
    readonly showDelete?:      boolean;
    readonly loading?:         boolean;
    readonly height?:          number | "grow";
    readonly onRenderToolbar?: (toolbar: ReactElement) => void;
}

//////////////////////////////////////////////////////////////////////////////
