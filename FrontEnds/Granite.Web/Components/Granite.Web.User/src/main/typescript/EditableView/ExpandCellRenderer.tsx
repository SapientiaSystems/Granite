//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement, MouseEvent } from "react";
import type { CustomCellRendererProps }  from "ag-grid-react";
import type { IUserRow }                 from "./UsersChangeSet";

export function ExpandCellRenderer(props: CustomCellRendererProps<IUserRow>): ReactElement | null
{
    if (props.data == null || props.data.isDetailRow)
    {
        return null;
    }

    const rowKey:       string                = props.data.rowKey;
    const toggleExpand: (key: string) => void = props.context.toggleExpand;
    const isExpanded:   boolean               = props.context.expandedKey === rowKey;

    function handleClick(e: MouseEvent): void
    {
        e.stopPropagation();
        toggleExpand(rowKey);
    }

    return (
        <span onClick={handleClick}
              style={{ cursor: "pointer", userSelect: "none",
                       display: "flex", alignItems: "center", justifyContent: "center",
                       width: "100%", height: "100%", fontSize: "0.82rem" }}>
            {isExpanded ? "▼" : "▶"}
        </span>
    );
}

//////////////////////////////////////////////////////////////////////////////
