//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { render }                 from "@testing-library/react";
import { MantineProvider }        from "@mantine/core";
import { MemoryRouter }           from "react-router-dom";
import type { ReactElement }      from "react";
import { AuthenticationProvider } from "granite.web.authentication/Authentication";
import type { IAuthentication }   from "granite.web.authentication/Authentication";

export function renderWithAuthentication(authentication: IAuthentication,path: string,routes: ReactElement): void
{
    render(
        <MantineProvider>
            <AuthenticationProvider authentication={authentication}>
                <MemoryRouter initialEntries={[path]}>
                    {routes}
                </MemoryRouter>
            </AuthenticationProvider>
        </MantineProvider>);
}

//////////////////////////////////////////////////////////////////////////////
