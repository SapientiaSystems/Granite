//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import type { RoleData }               from "granite.frontendcontroller.service/RequestReply";
import { roleOptions }                 from "granite.web.user/EditableView";
import type { IRoleOption }            from "granite.web.user/EditableView";

const ADMIN_ROLE: string = "ROLE_ADMIN";

export function clientRoles(catalog: Array<RoleData> | undefined): Array<RoleData>
{
    return (catalog ?? [])
        .filter(role => role.name !== ADMIN_ROLE)
        .map(role => ({ ...role, impliedRoles: (role.impliedRoles ?? []).filter(name => name !== ADMIN_ROLE) }));
}

export function clientAuthorityOptions(catalog: Array<RoleData> | undefined,current: Array<string>): Array<IRoleOption>
{
    return roleOptions(clientRoles(catalog),current);
}

//////////////////////////////////////////////////////////////////////////////
