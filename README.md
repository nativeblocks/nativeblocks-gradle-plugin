# Nativeblocks gradle plugin

The Nativeblocks gradle plugin is a tool that for uploading JSON generated
by [compiler](https://github.com/nativeblocks/compiler-android) to Nativeblocks servers

## How it works

Before starting we need to add necessary dependencies

```groovy
plugins {
    id("io.nativeblocks.nativeblocks-gradle-plugin").version("[latest-version]")
}
```

Then it needed to provide plugin arguments

```groovy
def nativeblocksProps = new Properties()
file("sample.nativeblocks.properties").withInputStream { props.load(it) }

nativeblocks {
    endpoint = nativeblocksProps.getProperty("endpoint").toString()
    authToken = nativeblocksProps.getProperty("authToken").toString()
    organizationId = nativeblocksProps.getProperty("organizationId").toString()
    integrationTypes = [IntegrationType.BLOCK, IntegrationType.ACTION]
    basePackageName = "io.nativeblocks.sampleapp"
    moduleName = "Demo"
}
```