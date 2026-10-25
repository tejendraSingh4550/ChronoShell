# ChronoShell 2040

A terminal-only Java command shell for checking time, converting between time zones, adding durations, and counting down to an instant.

## Requirements

- Java 17 or newer
- Maven 3.8 or newer

## Build and run

```powershell
mvn package
java -jar target/chronoshell-2040-1.0.0.jar
```

You can also launch it through Maven with `mvn exec:java` only after adding an exec plugin; the project intentionally has no runtime dependencies.

## Commands

Start with `help` inside the shell. Date-times use ISO-8601 with an explicit offset, durations use ISO-8601 duration notation, and zones use IANA IDs such as `Asia/Kolkata` or `Europe/Paris`.

```text
now [zone]
convert <date-time-with-offset> <target-zone>
add <date-time-with-offset> <duration>
countdown <instant>
zones [filter]
clear
about
help
exit
```
