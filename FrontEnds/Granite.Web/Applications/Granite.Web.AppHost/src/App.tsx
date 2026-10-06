//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AppHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }             from "react";
import { BrowserRouter, Routes, Route }  from "react-router-dom";
import { MenuBar }                       from "granite.web.navigator/Navigation";
import { HomeView }                      from "granite.web.navigator/Home";
import { UsersView }               from "granite.web.navigator/User";
import { HelpView }                      from "granite.web.navigator/Help";
import { AccountView }                      from "granite.web.navigator/Account";

export function App(): ReactElement
{
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<MenuBar />}>
                    <Route index              element={<HomeView />} />
                    <Route path="users" element={<UsersView />} />
                    <Route path="help"        element={<HelpView />} />
                    <Route path="user"        element={<AccountView />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

//////////////////////////////////////////////////////////////////////////////
