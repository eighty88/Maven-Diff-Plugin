# Maven Patcher

Make diff file (.patch)

## Usage

Install

```
mvn clean install
```

pom.xml
```xml
    <plugin>
        <groupId>com.github.eighty88</groupId>
        <artifactId>MvnPatcher</artifactId>
        <version>1.0-SNAPSHOT</version>
        
        <configuration>
            <root>${basedir}/def</root>
            <target>${basedir}/src</target>
            <patches>${basedir}/patches</patches>
        </configuration>
    </plugin>
```