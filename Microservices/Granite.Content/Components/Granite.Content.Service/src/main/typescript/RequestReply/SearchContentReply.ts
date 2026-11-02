import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ContentHitData } from "./ContentHitData";

export
interface SearchContentReply
    extends AbstractServiceReply
{
    hits?: Array<ContentHitData>;
}
