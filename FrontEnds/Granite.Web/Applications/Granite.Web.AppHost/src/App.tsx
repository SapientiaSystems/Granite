//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AppHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                             from "react";
import { BrowserRouter, Routes, Route }                  from "react-router-dom";
import { AuthenticationProvider, RequireAuthentication } from "granite.web.authentication/Authentication";
import { LoginView }                                     from "granite.web.authentication/Login";
import { MenuBar }                                       from "granite.web.navigator/Navigation";
import { HomeView }                                      from "granite.web.navigator/Home";
import { UsersView }                                     from "granite.web.navigator/User";
import { HelpView }                                      from "granite.web.navigator/Help";
import { AccountView }                                   from "granite.web.navigator/Account";

export function App(): ReactElement
{
    return (
        <AuthenticationProvider>
            <BrowserRouter>
                <Routes>
                    <Route element={<MenuBar />}>
                        <Route index        element={<HomeView />} />
                        <Route path="login" element={<LoginView />} />
                        <Route path="users" element={<RequireAuthentication><UsersView /></RequireAuthentication>} />
                        <Route path="help"  element={<HelpView />} />
                        <Route path="user"  element={<RequireAuthentication><AccountView /></RequireAuthentication>} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </AuthenticationProvider>
    );
}

//////////////////////////////////////////////////////////////////////////////
