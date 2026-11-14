//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import { useState }                    from "react";
import type { ReactElement }           from "react";
import { MultiSelect }                 from "@mantine/core";
import type { CustomCellEditorProps }  from "ag-grid-react";
import type { ClientOwnerData }        from "granite.frontendcontroller.service/RequestReply";
import type { IClientRow }             from "./ClientsChangeSet";

export function ownerLabel(userId: number,users: Array<ClientOwnerData> | undefined): string
{
    const name: string | null | undefined = users?.find(user => user.userId === userId)?.userName;

    return name === undefined || name === null || name === "" ? "#" + userId : name;
}

export function OwnersCellEditor(props: CustomCellEditorProps<IClientRow,Array<number>>): ReactElement
{
    const users: Array<ClientOwnerData> | undefined = props.context?.users;
    const [selected, setSelected]                   = useState<Array<string>>((props.value ?? []).map(String));
    const options                                    = (users ?? []).map(user => ({ value: String(user.userId), label: ownerLabel(user.userId,users) }));

    for (const id of selected)
    {
        if (!options.some(option => option.value === id))
            options.push({ value: id, label: "#" + id });
    }

    function handleChange(next: Array<string>): void
    {
        setSelected(next);
        props.onValueChange(next.map(Number));
    }

    return (
        <div style={{ padding: 6, minWidth: 320, background: "var(--mantine-color-body, #fff)" }}>
            <MultiSelect size="xs"
                         autoFocus
                         searchable
                         placeholder="select owners"
                         comboboxProps={{ withinPortal: false }}
                         data={options}
                         value={selected}
                         onChange={handleChange} />
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
