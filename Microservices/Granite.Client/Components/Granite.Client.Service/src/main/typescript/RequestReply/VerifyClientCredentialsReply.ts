import type { AbstractServiceReply } from "strata.foundation.core/Transfer";

export
interface VerifyClientCredentialsReply
    extends AbstractServiceReply
{
    valid?: boolean;
    clientId?: string;
    authorities?: Array<string>;
}
