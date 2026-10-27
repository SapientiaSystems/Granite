//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Account
//////////////////////////////////////////////////////////////////////////////

import { useCallback, useState } from "react";
import { usePreferences }        from "./PreferencesProvider";

export interface IPreferenceState
{
    saving: boolean;
    error:  string | null;
}

export function usePreference<T>(key: string): [T,(value: T) => Promise<void>,IPreferenceState]
{
    const { values, definitions, save } = usePreferences();
    const [pending, setPending]         = useState<{ value: T } | null>(null);
    const [saving, setSaving]           = useState<boolean>(false);
    const [error, setError]             = useState<string | null>(null);

    const stored: unknown =
        key in values ? values[key] : definitions.find(definition => definition.key === key)?.defaultValue;
    const value: T = (pending !== null ? pending.value : stored) as T;

    const update = useCallback(async (next: T): Promise<void> =>
    {
        setPending({ value: next });
        setSaving(true);
        setError(null);

        const failure: string | null = await save(key,next);

        setPending(null);
        setSaving(false);
        setError(failure);
    },[key,save]);

    return [value,update,{ saving, error }];
}

//////////////////////////////////////////////////////////////////////////////
