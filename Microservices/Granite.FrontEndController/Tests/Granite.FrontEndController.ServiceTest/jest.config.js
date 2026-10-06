module.exports =
{
    preset: "ts-jest",
    testEnvironment: "node",
    testMatch: ["<rootDir>/src/test/typescript/**/*Test.ts"],
    moduleNameMapper:
    {
        "^__system__\\.frontendcontroller\\.service/(.*)$":
            "<rootDir>/../../Components/__System__.FrontEndController.Service/src/main/typescript/$1",
        "^strata\\.foundation\\.core/(.*)$":
            "<rootDir>/../../Components/__System__.FrontEndController.Service/node_modules/strata.foundation.core/$1",
    },
};
