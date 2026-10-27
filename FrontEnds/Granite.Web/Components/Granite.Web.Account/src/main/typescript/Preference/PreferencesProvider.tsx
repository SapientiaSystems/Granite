//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Account
//////////////////////////////////////////////////////////////////////////////

import { createContext, useCallback, useContext, useEffect,
         useLayoutEffect, useMemo, useRef, useState }            from "react";
import type { ReactElement, ReactNode }                          from "react";
import { useMantineColorScheme }                                 from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }                from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient }          from "granite.frontendcontroller.client/RequestReply";
import type { GetPreferencesOutputReply, HandlePreferencesInputReply,
              PreferenceDefinitionData, PreferencesInput,
              PreferencesOutput }                                from "granite.frontendcontroller.service/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder }    from "strata.frontendcontroller.service/RequestReply";
import { useAuthentication }                                     from "granite.web.authentication/Authentication";
import { THEME_KEY, isTheme, readCachedTheme, writeCachedTheme } from "./ThemeCache";

const PREFERENCES_SOURCE:      string = "Preferences.Current";
const PREFERENCES_DESTINATION: string = "PreferencesView";
const PREFERENCES_SERVICE:     string = "frontendcontroller-user-service";

export type PreferencesStatus = "idle" | "loading" | "ready" | "error";

export interface IPreferencesContext
{
    values:      Record<string,unknown>;
    definitions: Array<PreferenceDefinitionData>;
    status:      PreferencesStatus;
    error:       string | null;
    save:        (key: string,value: unknown) => Promise<string | null>;
}

const EMPTY_CONTEXT: IPreferencesContext =
    { values: {}, definitions: [], status: "idle", error: null, save: async () => "Preferences are not available" };

export const PreferencesContext = createContext<IPreferencesContext>(EMPTY_CONTEXT);

export interface IPreferencesProviderProps
{
    children: ReactNode;
}

function messageOf(error: unknown): string
{
    return error instanceof Error ? error.message : "Request failed";
}

export function PreferencesProvider({ children }: IPreferencesProviderProps): ReactElement
{
    const { state, createFrontEndClient } = useAuthentication();
    const { setColorScheme }              = useMantineColorScheme();
    const [values, setValues]             = useState<Record<string,unknown>>({});
    const [definitions, setDefinitions]   = useState<Array<PreferenceDefinitionData>>([]);
    const [status, setStatus]             = useState<PreferencesStatus>("idle");
    const [error, setError]               = useState<string | null>(null);
    const authenticated: boolean          = state.status === "authenticated";
    const anonymous: boolean              = state.status === "anonymous";
    const userName: string | undefined    = state.userName;
    const createRef                       = useRef(createFrontEndClient);
    const schemeRef                       = useRef(setColorScheme);
    const loadedUser                      = useRef<string | undefined>(undefined);
    const saveCounter                     = useRef<number>(0);
    const latestSave                      = useRef<Map<string,number>>(new Map());
    const appliedSave                     = useRef<number>(0);

    createRef.current = createFrontEndClient;
    schemeRef.current = setColorScheme;

    const createClient = useCallback((): IGraniteFrontEndControllerServiceClient =>
        createRef.current((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),PREFERENCES_SERVICE),[]);

    useLayoutEffect(() =>
    {
        schemeRef.current(readCachedTheme());
    },[]);

    useEffect(() =>
    {
        if (anonymous)
        {
            loadedUser.current = undefined;
            setValues({});
            setDefinitions([]);
            setStatus("idle");
            setError(null);
            return;
        }

        if (!authenticated)
            return;

        if (loadedUser.current !== userName)
        {
            loadedUser.current = userName;
            setStatus("loading");
            setValues({});
            setDefinitions([]);
        }

        let cancelled: boolean = false;
        const client           = createClient();

        setStatus((current: PreferencesStatus) => current === "ready" ? current : "loading");

        (async () =>
        {
            try
            {
                const reply: GetPreferencesOutputReply =
                    await client.getPreferencesOutput(
                        GetOutputRequestBuilder.newBuilder().setSource(PREFERENCES_SOURCE).setDestination(PREFERENCES_DESTINATION).build());
                const output: PreferencesOutput | undefined = reply.output;

                if (cancelled)
                    return;

                if (reply.success === false || !output)
                {
                    setError(reply.failureMessage ?? "Failed to load preferences");
                    setStatus("error");
                    return;
                }

                setValues(output.values ?? {});
                setDefinitions(output.definitions ?? []);
                setError(null);
                setStatus("ready");
            }
            catch (failure)
            {
                if (!cancelled)
                {
                    setError(messageOf(failure));
                    setStatus("error");
                }
            }
            finally
            {
                client.close();
            }
        })();

        return () => { cancelled = true; };
    },[authenticated,anonymous,userName,createClient]);

    const serverTheme: unknown = values[THEME_KEY];

    useEffect(() =>
    {
        if (status === "ready" && isTheme(serverTheme))
        {
            setColorScheme(serverTheme);
            writeCachedTheme(serverTheme);
        }
    },[status,serverTheme,setColorScheme]);

    const save = useCallback(async (key: string,value: unknown): Promise<string | null> =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const id: number                                      = ++saveCounter.current;

        latestSave.current.set(key,id);

        try
        {
            const input: PreferencesInput =
                { userId: null, source: PREFERENCES_SOURCE, command: "Save", destination: PREFERENCES_DESTINATION, values: { [key]: value } };
            const reply: HandlePreferencesInputReply =
                await client.handlePreferencesInput(HandleInputRequestBuilder.newBuilder<PreferencesInput>().setInput(input).build());
            const output: PreferencesOutput | undefined = reply.output;

            if (reply.success === false || !output)
                return reply.failureMessage ?? "Failed to save preferences";

            if ((output.failureMessages ?? []).length > 0)
                return output.failureMessages.join("; ");

            if (id < appliedSave.current)
                return null;

            appliedSave.current = id;

            const newer: Array<string> =
                [...latestSave.current.entries()].filter(([,saveId]) => saveId !== id).map(([savedKey]) => savedKey);

            setValues((previous: Record<string,unknown>) =>
            {
                const next: Record<string,unknown> = { ...(output.values ?? {}) };

                newer.forEach((savedKey: string) => { next[savedKey] = previous[savedKey]; });

                return next;
            });
            setDefinitions(output.definitions ?? []);
            return null;
        }
        catch (failure)
        {
            return messageOf(failure);
        }
        finally
        {
            if (latestSave.current.get(key) === id)
                latestSave.current.delete(key);

            client.close();
        }
    },[createClient]);

    const context: IPreferencesContext = useMemo(() => ({ values, definitions, status, error, save }),[values,definitions,status,error,save]);

    return (
        <PreferencesContext.Provider value={context}>
            {children}
        </PreferencesContext.Provider>
    );
}

export function usePreferences(): IPreferencesContext
{
    return useContext(PreferencesContext);
}

//////////////////////////////////////////////////////////////////////////////
