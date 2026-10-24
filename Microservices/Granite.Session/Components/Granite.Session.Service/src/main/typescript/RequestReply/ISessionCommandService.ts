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
import {RefreshRequest} from "./RefreshRequest";
import {RefreshReply} from "./RefreshReply";
import {ResolveSessionRequest} from "./ResolveSessionRequest";
import {ResolveSessionReply} from "./ResolveSessionReply";

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

    refresh(request: RefreshRequest):
        ICompletionStage<RefreshReply>;

    resolveSession(request: ResolveSessionRequest):
        ICompletionStage<ResolveSessionReply>;
}
