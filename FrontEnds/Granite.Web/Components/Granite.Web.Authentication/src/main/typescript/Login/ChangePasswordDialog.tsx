//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                     from "react";
import type { FormEvent, ReactElement }            from "react";
import { Alert, Button, Group, Modal, PasswordInput,
         Stack, Text }                             from "@mantine/core";
import { useAuthentication }                       from "../Authentication/AuthenticationContext";
import type { IChangePasswordResult }              from "../Authentication/ISessionService";

export interface IChangePasswordDialogProps
{
    opened:  boolean;
    onClose: () => void;
}

const MISMATCH: string = "Passwords do not match";

export function ChangePasswordDialog({ opened, onClose }: IChangePasswordDialogProps): ReactElement
{
    const { changePassword }                = useAuthentication();
    const [current, setCurrent]             = useState<string>("");
    const [next, setNext]                   = useState<string>("");
    const [confirm, setConfirm]             = useState<string>("");
    const [submitting, setSubmitting]       = useState<boolean>(false);
    const [error, setError]                 = useState<string | undefined>(undefined);
    const [changed, setChanged]             = useState<boolean>(false);
    const filled: boolean                   = current.length > 0 && next.length > 0 && confirm.length > 0;
    const mismatch: boolean                 = confirm.length > 0 && next !== confirm;

    useEffect(() =>
    {
        if (opened)
            return;

        setCurrent("");
        setNext("");
        setConfirm("");
        setSubmitting(false);
        setError(undefined);
        setChanged(false);
    },[opened]);

    async function submit(event: FormEvent): Promise<void>
    {
        event.preventDefault();

        if (!filled || submitting)
            return;

        if (next !== confirm)
        {
            setError(MISMATCH);
            return;
        }

        setSubmitting(true);
        setError(undefined);

        const result: IChangePasswordResult = await changePassword(current,next);

        setSubmitting(false);

        if (result.success)
        {
            setCurrent("");
            setNext("");
            setConfirm("");
            setChanged(true);
        }
        else
            setError(result.message ?? "Unable to change password");
    }

    return (
        <Modal
            opened={opened}
            centered
            onClose={onClose}
            title="Change Password"
            transitionProps={{ duration: 0 }}
        >
            {changed
                ? <Stack>
                      <Text size="sm">Your password has been changed. Other sessions were signed out.</Text>
                      <Group justify="flex-end">
                          <Button onClick={onClose}>Close</Button>
                      </Group>
                  </Stack>
                : <form onSubmit={(event: FormEvent) => { void submit(event); }}>
                      <Stack>
                          <PasswordInput
                              label="Current password"
                              autoComplete="current-password"
                              value={current}
                              onChange={(event) => setCurrent(event.currentTarget.value)}
                          />
                          <PasswordInput
                              label="New password"
                              autoComplete="new-password"
                              value={next}
                              onChange={(event) => setNext(event.currentTarget.value)}
                          />
                          <PasswordInput
                              label="Confirm new password"
                              autoComplete="new-password"
                              value={confirm}
                              onChange={(event) => setConfirm(event.currentTarget.value)}
                              error={mismatch ? MISMATCH : undefined}
                          />
                          {error !== undefined && !(mismatch && error === MISMATCH) &&
                              <Alert color="red" role="alert">{error}</Alert>}
                          <Group justify="flex-end">
                              <Button type="submit" loading={submitting} disabled={!filled}>Change Password</Button>
                          </Group>
                      </Stack>
                  </form>}
        </Modal>
    );
}

//////////////////////////////////////////////////////////////////////////////
