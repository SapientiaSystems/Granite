//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Session
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }            from "react";
import type { CustomCellRendererProps } from "ag-grid-react";
import type { RoleData }                from "granite.frontendcontroller.service/RequestReply";
import { roleLabel }                    from "./SessionRows";
import type { ISessionRow }             from "./SessionRows";

const chipStyle = { display: "inline-block", background: "#dde7f7", color: "#1d3d70", borderRadius: 10,
                    padding: "1px 8px", margin: "1px 2px", fontSize: 12, lineHeight: "18px" };

export function AuthoritiesCellRenderer(props: CustomCellRendererProps<ISessionRow>): ReactElement | null
{
    if (props.data == null || props.data.isDetailRow)
    {
        return null;
    }

    const authorities: Array<string>               = [...(props.data.authorities ?? [])].sort();
    const roles:       Array<RoleData> | undefined = props.context.roles;

    return (
        <span>
            {authorities.map(a => <span key={a} style={chipStyle}>{roleLabel(a,roles)}</span>)}
        </span>
    );
}

//////////////////////////////////////////////////////////////////////////////
