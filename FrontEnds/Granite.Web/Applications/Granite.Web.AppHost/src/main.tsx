//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AppHost
//////////////////////////////////////////////////////////////////////////////

import { StrictMode }        from "react";
import { createRoot }        from "react-dom/client";
import { MantineProvider }   from "@mantine/core";
import { graniteTheme }   from "./theme";
import { App }               from "./App";
import "@mantine/core/styles.css";
import "./style.css";

createRoot(document.getElementById("app")!).render(
    <StrictMode>
        <MantineProvider theme={graniteTheme} defaultColorScheme="dark">
            <App />
        </MantineProvider>
    </StrictMode>,
);

//////////////////////////////////////////////////////////////////////////////
