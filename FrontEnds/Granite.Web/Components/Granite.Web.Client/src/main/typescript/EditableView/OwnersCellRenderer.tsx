//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }             from "react";
import type { CustomCellRendererProps }  from "ag-grid-react";
import type { ClientOwnerData }          from "granite.frontendcontroller.service/RequestReply";
import type { IClientRow }               from "./ClientsChangeSet";
import { ownerLabel }                    from "./OwnersCellEditor";

const chipStyle = { display: "inline-block", background: "#e3f2e6", color: "#1f4d2b", borderRadius: 10,
                    padding: "1px 8px", margin: "1px 2px", fontSize: 12, lineHeight: "18px" };

export function OwnersCellRenderer(props: CustomCellRendererProps<IClientRow>): ReactElement | null
{
    if (props.data == null)
        return null;

    const users: Array<ClientOwnerData> | undefined = props.context.users;

    return (
        <span>
            {(props.data.owners ?? []).map(id => <span key={id} style={chipStyle}>{ownerLabel(id,users)}</span>)}
        </span>
    );
}

//////////////////////////////////////////////////////////////////////////////
