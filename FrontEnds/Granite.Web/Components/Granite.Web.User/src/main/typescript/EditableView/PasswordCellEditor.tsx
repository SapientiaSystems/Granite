//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef }          from "react";
import type { ReactElement }          from "react";
import type { CustomCellEditorProps } from "ag-grid-react";
import type { IUserRow }              from "./UsersChangeSet";

export function PasswordCellEditor(props: CustomCellEditorProps<IUserRow, string>): ReactElement
{
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() =>
    {
        inputRef.current?.focus();
    }, []);

    return (
        <input ref={inputRef}
               type="password"
               autoComplete="new-password"
               value={props.value ?? ""}
               onChange={(e) => props.onValueChange(e.target.value)}
               style={{ width: "100%", height: "100%", boxSizing: "border-box", border: "none", padding: "0 8px" }} />
    );
}

//////////////////////////////////////////////////////////////////////////////
