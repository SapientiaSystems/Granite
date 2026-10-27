//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                                 from "react";
import type { UserData, CreateUserData, UpdateUserData }     from "granite.user.service/RequestReply";
import type { RoleData }                                     from "granite.frontendcontroller.service/RequestReply";

export interface IUsersResult
{
    users:           Array<UserData>;
    failureMessages: Array<string>;
}

export interface IUsersEditableViewProps
{
    readonly users?:           Array<UserData>;
    readonly roles?:           Array<RoleData>;
    readonly currentUserId?:   number;
    readonly showCreate?:      boolean;
    readonly showEdit?:        boolean;
    readonly showSave?:        boolean;
    readonly showUndo?:        boolean;
    readonly showDelete?:      boolean;
    readonly loading?:         boolean;
    readonly height?:          number | "grow";
    readonly onRenderToolbar?: (toolbar: ReactElement) => void;
    readonly onSave?:          (changes: { usersToCreate: Array<CreateUserData>; usersToUpdate: Array<UpdateUserData> }) => Promise<IUsersResult>;
    readonly onDelete?:        (userIds: Array<number>) => Promise<IUsersResult>;
}

//////////////////////////////////////////////////////////////////////////////
