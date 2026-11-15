//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                             from "react";
import { BrowserRouter, Routes, Route }                  from "react-router-dom";
import { AuthenticationProvider, RequireAuthentication } from "granite.web.authentication/Authentication";
import { LoginCallbackView, LoginView }                  from "granite.web.authentication/Login";
import { PreferencesProvider }                           from "granite.web.account/Preference";
import { OwnedClientsView }                              from "granite.web.client/Owned";
import { ClientHostFrame }                               from "./ClientHostFrame";
import { ClientHostPreferencesView }                     from "./ClientHostPreferencesView";

export function App(): ReactElement
{
    return (
        <AuthenticationProvider>
            <PreferencesProvider>
                <BrowserRouter>
                    <Routes>
                        <Route element={<ClientHostFrame />}>
                            <Route index                 element={<RequireAuthentication><OwnedClientsView /></RequireAuthentication>} />
                            <Route path="login"          element={<LoginView />} />
                            <Route path="login/callback" element={<LoginCallbackView />} />
                            <Route path="preferences"    element={<RequireAuthentication><ClientHostPreferencesView /></RequireAuthentication>} />
                        </Route>
                    </Routes>
                </BrowserRouter>
            </PreferencesProvider>
        </AuthenticationProvider>
    );
}

//////////////////////////////////////////////////////////////////////////////
