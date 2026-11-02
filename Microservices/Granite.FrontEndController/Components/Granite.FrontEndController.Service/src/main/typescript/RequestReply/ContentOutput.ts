//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";
import type { ContentDocumentData, ContentHitData } from "granite.content.service/RequestReply";

export
interface ContentOutput
    extends FrontEndOutput
{
    index: Array<ContentHitData>;
    hits: Array<ContentHitData>;
    article?: ContentDocumentData | null;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
