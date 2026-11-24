//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AppHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                             from "react";
import { BrowserRouter, Routes, Route }                  from "react-router-dom";
import { AuthenticationProvider, RequireAuthentication } from "granite.web.authentication/Authentication";
import { LoginCallbackView, LoginView }                  from "granite.web.authentication/Login";
import { PreferencesProvider }                           from "granite.web.account/Preference";
import { MenuBar }                                       from "granite.web.navigator/Navigation";
import { HomeView }                                      from "granite.web.navigator/Home";
import { UsersView }                                     from "granite.web.user/Users";
import { ClientsView }                                   from "granite.web.client/Clients";
import { SessionsView }                                  from "granite.web.session/Sessions";
import { FeedbackReviewView }                            from "granite.web.feedback/Review";
import { SurveyReviewView }                              from "granite.web.survey/Review";
import { HelpView }                                      from "granite.web.navigator/Help";
import { AccountView }                                   from "granite.web.navigator/Account";

export function App(): ReactElement
{
    return (
        <AuthenticationProvider>
            <PreferencesProvider>
                <BrowserRouter>
                    <Routes>
                        <Route element={<MenuBar />}>
                            <Route index        element={<HomeView />} />
                            <Route path="login" element={<LoginView />} />
                            <Route path="login/callback" element={<LoginCallbackView />} />
                            <Route path="users" element={<RequireAuthentication><UsersView /></RequireAuthentication>} />
                            <Route path="clients" element={<RequireAuthentication><ClientsView /></RequireAuthentication>} />
                            <Route path="sessions" element={<RequireAuthentication><SessionsView /></RequireAuthentication>} />
                            <Route path="feedback" element={<RequireAuthentication><FeedbackReviewView /></RequireAuthentication>} />
                            <Route path="surveys" element={<RequireAuthentication><SurveyReviewView /></RequireAuthentication>} />
                            <Route path="help"  element={<HelpView />} />
                            <Route path="user"  element={<RequireAuthentication><AccountView /></RequireAuthentication>} />
                        </Route>
                    </Routes>
                </BrowserRouter>
            </PreferencesProvider>
        </AuthenticationProvider>
    );
}

//////////////////////////////////////////////////////////////////////////////
