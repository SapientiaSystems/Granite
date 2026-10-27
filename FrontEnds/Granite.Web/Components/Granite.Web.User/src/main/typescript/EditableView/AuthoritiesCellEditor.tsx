//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { useState }                    from "react";
import type { ReactElement }           from "react";
import { MultiSelect }                 from "@mantine/core";
import type { CustomCellEditorProps }  from "ag-grid-react";
import type { RoleData }               from "granite.frontendcontroller.service/RequestReply";
import { normalizeAuthorities }        from "./UsersChangeSet";
import type { IUserRow }               from "./UsersChangeSet";
import { applySelection, roleOptions } from "./RoleCascade";

export function AuthoritiesCellEditor(props: CustomCellEditorProps<IUserRow, Array<string>>): ReactElement
{
    const [selected, setSelected]            = useState<Array<string>>(props.value ?? []);
    const catalog: Array<RoleData> | undefined = props.context?.roles;

    function handleChange(next: Array<string>): void
    {
        const result: Array<string> = normalizeAuthorities(applySelection(selected,next,catalog));

        setSelected(result);
        props.onValueChange(result);
    }

    return (
        <div style={{ padding: 6, minWidth: 320, background: "var(--mantine-color-body, #fff)" }}>
            <MultiSelect size="xs"
                         autoFocus
                         searchable
                         placeholder="select roles"
                         comboboxProps={{ withinPortal: false }}
                         data={roleOptions(catalog,selected)}
                         value={selected}
                         onChange={handleChange} />
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
