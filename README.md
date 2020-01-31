# MAL (the Meta Attack Language)

MAL is a language for designing cyber threat modeling languages for
specific domains. For more information, see <https://mal-lang.org/>.

Some examples of MAL languages are:
* [exampleLang](https://github.com/mal-lang/exampleLang)
* [vehicleLang](https://github.com/mal-lang/vehicleLang)

## Creating a MAL language:

For information on how to set up your development environment for
creating MAL languages, please refer to the `README.md` file in
exampleLang:
<https://github.com/mal-lang/exampleLang/blob/master/README.md>

For MAL language development, you don't need to compile the
MAL compiler yourself. It will be downloaded by maven automatically.

## Developing the MAL compiler

To develop the MAL compiler, you need to have Apache Maven and a Java
JDK installed. You also need to enable the OSSRH snapshot repository.
You can do that by creating a file `~/.m2/settings.xml` with the
following content:

```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <profiles>
    <profile>
      <id>ossrh-snapshots</id>
      <repositories>
        <repository>
          <id>ossrh</id>
          <url>https://oss.sonatype.org/content/repositories/snapshots</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>ossrh-snapshots</activeProfile>
  </activeProfiles>
</settings>
```

Execute the following steps to download and compile the MAL complier:

```
git clone git://github.com/mal-lang/malcompiler.git
cd malcomplier
mvn install
```
