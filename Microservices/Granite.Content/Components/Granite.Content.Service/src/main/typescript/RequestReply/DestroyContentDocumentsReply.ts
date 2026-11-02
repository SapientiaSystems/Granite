import type { AbstractServiceReply } from "strata.foundation.core/Transfer";

export
interface DestroyContentDocumentsReply
    extends AbstractServiceReply
{
    requestedKeys?: Array<string>;
    failedKeys?: Array<string>;
    destroyedKeys?: Array<string>;
}
