# Nativeblocks gradle plugin

The Nativeblocks gradle plugin is a tool that for uploading JSON generated and prepare schema
by [compiler](https://github.com/nativeblocks/compiler-android) to Nativeblocks servers

## How it works

Before starting we need to add necessary dependencies

```groovy
plugins {
    id("io.nativeblocks.nativeblocks-gradle-plugin").version("[latest-version]")
}
```

Then it needed to provide plugin arguments,
Properties belongs to each Studio account, from Nativeblocks Studio, find **Link Device** and copy properties

```properties
# nativeblocks.properties
endpoint=
authToken=
organizationId=
```

Note: if you are using community version, there is no need to add endpoint, authToken and organizationId

```groovy
def nativeblocksProps = new Properties()
file("nativeblocks.properties").withInputStream { props.load(it) }

nativeblocks {
    endpoint = nativeblocksProps.getProperty("endpoint").toString()
    authToken = nativeblocksProps.getProperty("authToken").toString()
    organizationId = nativeblocksProps.getProperty("organizationId").toString()
    basePackageName = "your.packagename.appname"
    moduleName = "your_module_name"
}
```

#### Sync
```shell
 ./gradlew :app:nativeblocksSyncDebug
```

#### PrepareSchema
```shell
 ./gradlew :app:nativeblocksPrepareSchemaDebug
```