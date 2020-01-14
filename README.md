# webpack Gradle Plugin

## Requirements

* Gradle >= 6.0

## Usage

First, apply the plugin:
```groovy
plugins {
    id 'com.github.dtrunk90' version '1.0.0'
}
```
Optionally, configure it for your needs (default values shown):

```groovy
webpack {
    // node version to use
    nodeVersion = "v12.14.1"

    // webpack version to use
    webpackVersion = "4.41.5"

    // Directory where to install node
    directory = file("${rootDir}/.gradle/node")

    // Base URL where to download node from
    baseUrl = "https://nodejs.org/dist"
}
```
You may also configure the `execWebpack` task:

```groovy
execWebpack {
    // webpack config file
    configFile = file("${projectDir}/webpack.config.js")
}
```
