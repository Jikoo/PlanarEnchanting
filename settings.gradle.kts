rootProject.name = "planarenchanting-parent"

include(":planarenchanting")
project(":planarenchanting").projectDir = file("enchanting-core")

include(":planarenchanting-generator")
project(":planarenchanting-generator").projectDir = file("enchanting-generator")

startParameter.excludedTaskNames.add(":planarenchanting-generator:build")
