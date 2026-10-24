import {ICompletionStage} from "strata.foundation.core/Concurrent";
import {LoginRequest} from "./LoginRequest";
import {LoginReply} from "./LoginReply";
import {LoginWithOauth2Request} from "./LoginWithOauth2Request";
import {LoginWithOauth2Reply} from "./LoginWithOauth2Reply";
import {LogoutRequest} from "./LogoutRequest";
import {LogoutReply} from "./LogoutReply";
import {LogoutAllForUserRequest} from "./LogoutAllForUserRequest";
import {LogoutAllForUserReply} from "./LogoutAllForUserReply";
import {RefreshRequest} from "./RefreshRequest";
import {RefreshReply} from "./RefreshReply";
import {ResolveSessionRequest} from "./ResolveSessionRequest";
import {ResolveSessionReply} from "./ResolveSessionReply";

export
interface ISessionCommandService
{
    login(request: LoginRequest):
        ICompletionStage<LoginReply>;

    loginWithOauth2(request: LoginWithOauth2Request):
        ICompletionStage<LoginWithOauth2Reply>;

    logout(request: LogoutRequest):
        ICompletionStage<LogoutReply>;

    logoutAllForUser(request: LogoutAllForUserRequest):
        ICompletionStage<LogoutAllForUserReply>;

    refresh(request: RefreshRequest):
        ICompletionStage<RefreshReply>;

    resolveSession(request: ResolveSessionRequest):
        ICompletionStage<ResolveSessionReply>;
}
