//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }          from "react";
import { TagsInput }                  from "@mantine/core";
import type { CustomCellEditorProps } from "ag-grid-react";
import { normalizeAuthorities }       from "./UsersChangeSet";
import type { IUserRow }              from "./UsersChangeSet";

export function AuthoritiesCellEditor(props: CustomCellEditorProps<IUserRow, Array<string>>): ReactElement
{
    return (
        <div style={{ padding: 6, minWidth: 320, background: "var(--mantine-color-body, #fff)" }}>
            <TagsInput size="xs"
                       autoFocus
                       placeholder="type an authority and press Enter"
                       splitChars={[","]}
                       value={props.value ?? []}
                       onChange={(v: Array<string>) => props.onValueChange(normalizeAuthorities(v))} />
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
