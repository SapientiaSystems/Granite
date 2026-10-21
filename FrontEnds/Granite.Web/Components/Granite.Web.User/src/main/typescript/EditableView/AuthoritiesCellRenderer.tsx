//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement, MouseEvent } from "react";
import type { CustomCellRendererProps }  from "ag-grid-react";
import type { IUserRow }                 from "./UsersChangeSet";

const chipStyle = { display: "inline-block", background: "#dde7f7", color: "#1d3d70", borderRadius: 10,
                    padding: "1px 8px", margin: "1px 2px", fontSize: 12, lineHeight: "18px" };

export function AuthoritiesCellRenderer(props: CustomCellRendererProps<IUserRow>): ReactElement | null
{
    if (props.data == null || props.data.isDetailRow)
    {
        return null;
    }

    const rowKey:      string                                   = props.data.rowKey;
    const editing:     boolean                                  = props.context.isEditing(rowKey) as boolean;
    const remove:      (key: string, authority: string) => void = props.context.removeAuthority;
    const authorities: Array<string>                            = props.data.authorities ?? [];

    function handleRemove(e: MouseEvent, authority: string): void
    {
        e.stopPropagation();
        remove(rowKey, authority);
    }

    return (
        <span>
            {authorities.map(a => (
                <span key={a} style={chipStyle}>
                    {a}
                    {editing && <b style={{ marginLeft: 4, cursor: "pointer" }} onClick={(e) => handleRemove(e, a)}>{"×"}</b>}
                </span>
            ))}
            {editing && <span style={{ color: "#777", fontSize: 12 }}> double-click to add...</span>}
        </span>
    );
}

//////////////////////////////////////////////////////////////////////////////
