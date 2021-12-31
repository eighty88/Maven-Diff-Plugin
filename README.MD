# Maven Patcher

Make / Apply diff file (.patch)

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
            <root>${basedir}/root</root>
            <target>${basedir}/src</target>
            <patches>${basedir}/patches</patches>
        </configuration>
    </plugin>
```