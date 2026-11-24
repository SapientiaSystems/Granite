//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientHost
//////////////////////////////////////////////////////////////////////////////

import { createTheme }               from "@mantine/core";
import type { MantineThemeOverride } from "@mantine/core";

export const graniteTheme: MantineThemeOverride = createTheme({
    primaryColor: "graniteBrand",
    colors: {
        graniteBrand: [
            "#ffeef1", "#fddce1", "#f6b7bf", "#f08e9b",
            "#eb6c7d", "#e94560", "#e84555",
            "#ce3747", "#b92d3d", "#a22032",
        ],
        dark: [
            "#d4d4d4",
            "#b8b8b8",
            "#999999",
            "#7a7a7a",
            "#636363",
            "#555555",
            "#484848",
            "#424242",
            "#2a2a2a",
            "#222222",
        ],
    },
    fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif",
    fontSizes: {
        xs: "0.7rem",
        sm: "0.8rem",
        md: "0.875rem",
        lg: "0.975rem",
        xl: "1.1rem",
    },
});

//////////////////////////////////////////////////////////////////////////////
