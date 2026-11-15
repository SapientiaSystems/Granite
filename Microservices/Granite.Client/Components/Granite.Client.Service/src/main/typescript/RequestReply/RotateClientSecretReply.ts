import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ClientData } from "./ClientData";

export
interface RotateClientSecretReply
    extends AbstractServiceReply
{
    client?: ClientData;
    clientSecret?: string;
}
