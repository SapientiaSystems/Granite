//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import { useState }                    from "react";
import type { ReactElement }           from "react";
import { Button, Group, TextInput }    from "@mantine/core";
import type { CustomCellEditorProps }  from "ag-grid-react";
import type { IClientRow }             from "./ClientsChangeSet";

export function SecretExpiryCellEditor(props: CustomCellEditorProps<IClientRow,string | null>): ReactElement
{
    const [date, setDate] = useState<string>(props.value ?? "");

    function handleChange(next: string): void
    {
        setDate(next);
        props.onValueChange(next === "" ? null : next);
    }

    return (
        <div style={{ padding: 6, minWidth: 260, background: "var(--mantine-color-body, #fff)" }}>
            <Group gap="xs" wrap="nowrap">
                <TextInput size="xs" type="date" autoFocus value={date} onChange={event => handleChange(event.currentTarget.value)} />
                <Button size="compact-xs" variant="default" onClick={() => handleChange("")}>No expiry</Button>
            </Group>
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
