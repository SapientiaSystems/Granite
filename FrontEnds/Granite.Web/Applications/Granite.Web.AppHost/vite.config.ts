import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-oxc";

export default defineConfig({
    plugins: [react()],
    resolve: {
        dedupe: [
            "@mantine/core",
            "@mantine/hooks",
            "react",
            "react-dom",
            "ag-grid-community",
            "ag-grid-react",
        ],
    },
    server: {
        proxy: {
            "/frontendcontroller-user-service": {
                target:       "https://localhost:8181",
                changeOrigin: true,
                secure:       false,
            },
            "/frontendcontroller-admin-service": {
                target:       "https://localhost:8182",
                changeOrigin: true,
                secure:       false,
            },
        },
    },
});
