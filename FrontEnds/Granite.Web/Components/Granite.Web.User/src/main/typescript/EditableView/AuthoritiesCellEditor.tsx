//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }          from "react";
import { MultiSelect }                from "@mantine/core";
import type { CustomCellEditorProps } from "ag-grid-react";
import type { RoleData }              from "granite.frontendcontroller.service/RequestReply";
import { normalizeAuthorities }       from "./UsersChangeSet";
import type { IUserRow }              from "./UsersChangeSet";
import { applySelection, roleOptions } from "./RoleCascade";

export function AuthoritiesCellEditor(props: CustomCellEditorProps<IUserRow, Array<string>>): ReactElement
{
    const value:   Array<string>              = props.value ?? [];
    const catalog: Array<RoleData> | undefined = props.context?.roles;

    return (
        <div style={{ padding: 6, minWidth: 320, background: "var(--mantine-color-body, #fff)" }}>
            <MultiSelect size="xs"
                         autoFocus
                         searchable
                         placeholder="select roles"
                         data={roleOptions(catalog,value)}
                         value={value}
                         onChange={(v: Array<string>) => props.onValueChange(normalizeAuthorities(applySelection(value,v,catalog)))} />
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
