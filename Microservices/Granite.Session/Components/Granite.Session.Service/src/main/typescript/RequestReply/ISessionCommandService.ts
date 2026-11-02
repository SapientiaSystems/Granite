import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {LoginRequest} from "./LoginRequest";
import {LoginReply} from "./LoginReply";
import {StartOktaLoginRequest} from "./StartOktaLoginRequest";
import {StartOktaLoginReply} from "./StartOktaLoginReply";
import {CompleteOktaLoginRequest} from "./CompleteOktaLoginRequest";
import {CompleteOktaLoginReply} from "./CompleteOktaLoginReply";
import {LogoutRequest} from "./LogoutRequest";
import {LogoutReply} from "./LogoutReply";
import {LogoutAllForUserRequest} from "./LogoutAllForUserRequest";
import {LogoutAllForUserReply} from "./LogoutAllForUserReply";
import {CloseSessionsRequest} from "./CloseSessionsRequest";
import {CloseSessionsReply} from "./CloseSessionsReply";
import {RefreshRequest} from "./RefreshRequest";
import {RefreshReply} from "./RefreshReply";
import {ResolveSessionRequest} from "./ResolveSessionRequest";
import {ResolveSessionReply} from "./ResolveSessionReply";
import {IssueServiceTokenRequest} from "./IssueServiceTokenRequest";
import {IssueServiceTokenReply} from "./IssueServiceTokenReply";
import {IssueDelegatedTokenRequest} from "./IssueDelegatedTokenRequest";
import {IssueDelegatedTokenReply} from "./IssueDelegatedTokenReply";

export
interface ISessionCommandService
{
    login(request: LoginRequest):
        ICompletionStage<LoginReply>;

    startOktaLogin(request: StartOktaLoginRequest):
        ICompletionStage<StartOktaLoginReply>;

    completeOktaLogin(request: CompleteOktaLoginRequest):
        ICompletionStage<CompleteOktaLoginReply>;

    logout(request: LogoutRequest):
        ICompletionStage<LogoutReply>;

    logoutAllForUser(request: LogoutAllForUserRequest):
        ICompletionStage<LogoutAllForUserReply>;

    closeSessions(request: CloseSessionsRequest):
        ICompletionStage<CloseSessionsReply>;

    refresh(request: RefreshRequest):
        ICompletionStage<RefreshReply>;

    resolveSession(request: ResolveSessionRequest):
        ICompletionStage<ResolveSessionReply>;

    issueServiceToken(request: IssueServiceTokenRequest):
        ICompletionStage<IssueServiceTokenReply>;

    issueDelegatedToken(request: IssueDelegatedTokenRequest):
        ICompletionStage<IssueDelegatedTokenReply>;
}
