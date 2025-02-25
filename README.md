# maven-pom-recorder
Maven instrumentation agent for recording access of `pom.xml` files within the `~/.m2/repository` folder; in other word which artifacts are in use.

Results are written to `~/.m2/maven-pom-recorder-XYZ.txt`, where `XYZ` is a random string (per process).

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

### Help wanted! 
Do you know a better way to transparently get hold of the POMs which are in use in a build, let me know. 

 * Filesystem `atime` does not work (because many filesystems are mounted without this active).

## Obtain
The project is implemented in Java and built using [Maven]. The project is available on the central Maven repository.

## Usage
```
export MAVEN_OPTS="$MAVEN_OPTS -javaagent:/path/to/agent.jar"
```

The agent modifies the source code of the [FileSource](https://github.com/apache/maven/blob/master/maven-builder-support/src/main/java/org/apache/maven/building/FileSource.java) so that file paths are forwarded to the recorder.

## Shaded contents

 * ASM

## License
[Apache 2.0]

# History

 - 1.0.1: Make .m2 directory configurable via system property or environment variable `POM_RECORDER_M2_DIR`
 - 1.0.0: Initial release
 
[Apache 2.0]:           http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:        https://github.com/skjolber/maven-pom-recorder/issues
[Maven]:                http://maven.apache.org/
[ASM]:                  https://asm.ow2.io/
 