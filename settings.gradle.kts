rootProject.name = "KLambda"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
include(
    "infrastructure",

    "lib",

    ":aws:dynamoDB",
    ":aws:s3",

    "function:auth:login",
    "function:auth:register",

    "function:user:getUserById",
    "function:user:deleteUser",
    "function:user:updateUser",
)
