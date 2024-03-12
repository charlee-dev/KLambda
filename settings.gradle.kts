rootProject.name = "KLambda"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
include(
    "infrastructure",
    "lib",
)
include(
    "function:auth:login",
    "function:auth:register",

    "function:user:getUserById",
    "function:user:deleteUser",
    "function:user:updateUser",
)
