import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { CreateClientReply } from "./CreateClientReply";
import type { CreateClientRequest } from "./CreateClientRequest";
import type { DestroyClientsReply } from "./DestroyClientsReply";
import type { DestroyClientsRequest } from "./DestroyClientsRequest";
import type { RotateClientSecretReply } from "./RotateClientSecretReply";
import type { RotateClientSecretRequest } from "./RotateClientSecretRequest";
import type { RotateOwnClientSecretReply } from "./RotateOwnClientSecretReply";
import type { RotateOwnClientSecretRequest } from "./RotateOwnClientSecretRequest";
import type { RotateOwnedClientSecretRequest } from "./RotateOwnedClientSecretRequest";
import type { UpdateClientsReply } from "./UpdateClientsReply";
import type { UpdateClientsRequest } from "./UpdateClientsRequest";
import type { VerifyClientCredentialsReply } from "./VerifyClientCredentialsReply";
import type { VerifyClientCredentialsRequest } from "./VerifyClientCredentialsRequest";

export
interface IClientCommandService
{
    createClient(request: CreateClientRequest): ICompletionStage<CreateClientReply>;

    updateClients(request: UpdateClientsRequest): ICompletionStage<UpdateClientsReply>;

    rotateClientSecret(request: RotateClientSecretRequest): ICompletionStage<RotateClientSecretReply>;

    rotateOwnedClientSecret(request: RotateOwnedClientSecretRequest): ICompletionStage<RotateClientSecretReply>;

    rotateOwnClientSecret(request: RotateOwnClientSecretRequest): ICompletionStage<RotateOwnClientSecretReply>;

    destroyClients(request: DestroyClientsRequest): ICompletionStage<DestroyClientsReply>;

    verifyClientCredentials(request: VerifyClientCredentialsRequest): ICompletionStage<VerifyClientCredentialsReply>;
}
