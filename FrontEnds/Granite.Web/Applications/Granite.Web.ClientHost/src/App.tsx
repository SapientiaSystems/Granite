//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                             from "react";
import { BrowserRouter, Routes, Route }                  from "react-router-dom";
import { AuthenticationProvider, RequireAuthentication } from "granite.web.authentication/Authentication";
import { LoginCallbackView, LoginView }                  from "granite.web.authentication/Login";
import { OwnedClientsView }                              from "granite.web.client/Owned";
import { ClientHostHeader }                              from "./ClientHostHeader";

export function App(): ReactElement
{
    return (
        <AuthenticationProvider>
            <BrowserRouter>
                <ClientHostHeader />
                <Routes>
                    <Route path="login" element={<LoginView />} />
                    <Route path="login/callback" element={<LoginCallbackView />} />
                    <Route path="/" element={<RequireAuthentication><OwnedClientsView /></RequireAuthentication>} />
                </Routes>
            </BrowserRouter>
        </AuthenticationProvider>
    );
}

//////////////////////////////////////////////////////////////////////////////
