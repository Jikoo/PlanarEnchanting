rootProject.name = "planarenchanting-parent"

include("enchanting-generator")
include("enchanting-common", "enchanting-components", "enchanting-meta")

include(":planarenchanting")
project(":planarenchanting").projectDir = file("enchanting-bundler")

// Don't build generator unless generating.
startParameter.excludedTaskNames.add(":enchanting-generator:build")
