rootProject.name = "KLambda"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
include(
    "infrastructure",
    "function:first",
)
