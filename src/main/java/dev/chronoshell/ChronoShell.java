package dev.chronoshell;

import java.time.Duration;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Interactive terminal-only time and time-zone shell. */
public final class ChronoShell {
    private static final String PROMPT = "chrono> ";

    private ChronoShell() { }

    public static void main(String[] args) {
        printBanner();
        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                System.out.print(PROMPT);
                if (!input.hasNextLine()) {
                    System.out.println();
                    break;
                }
                String line = input.nextLine().trim();
                if (line.isEmpty()) continue;
                try {
                    if (!execute(line)) break;
                } catch (DateTimeParseException | IllegalArgumentException e) {
                    System.out.println("Input error: " + e.getMessage());
                }
            }
        }
        System.out.println("ChronoShell closed. Keep your timelines tidy.");
    }

    /** Returns false when the shell should stop. */
    private static boolean execute(String line) {
        String[] parts = line.split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);
        switch (command) {
            case "help", "?" -> printHelp();
            case "now" -> showNow(parts);
            case "convert" -> convert(parts);
            case "add" -> add(parts);
            case "countdown" -> countdown(parts);
            case "zones" -> zones(parts);
            case "clear" -> clear();
            case "about" -> System.out.println("ChronoShell 2040 — a small terminal toolkit for working across time.");
            case "exit", "quit" -> { return false; }
            default -> System.out.println("Unknown command: " + parts[0] + ". Type 'help' to see available commands.");
        }
        return true;
    }

    private static void showNow(String[] parts) {
        requireCount(parts, 1, 2, "now [zone]");
        ZoneId zone = parts.length == 2 ? zone(parts[1]) : ZoneId.systemDefault();
        System.out.println(ZonedDateTime.now(zone));
        System.out.println("Zone: " + zone.getId());
    }

    private static void convert(String[] parts) {
        requireCount(parts, 3, 3, "convert <ISO-8601 date-time with offset> <target-zone>");
        ZonedDateTime source = ZonedDateTime.parse(parts[1]);
        ZoneId target = zone(parts[2]);
        System.out.println(source + "  →  " + source.withZoneSameInstant(target));
    }

    private static void add(String[] parts) {
        requireCount(parts, 3, 3, "add <ISO-8601 date-time with offset> <ISO-8601 duration>");
        ZonedDateTime start = ZonedDateTime.parse(parts[1]);
        Duration duration = Duration.parse(parts[2]);
        System.out.println(start + " + " + duration + " = " + start.plus(duration));
    }

    private static void countdown(String[] parts) {
        requireCount(parts, 2, 2, "countdown <ISO-8601 instant, for example 2040-01-01T00:00:00Z>");
        Instant target = Instant.parse(parts[1]);
        Duration remaining = Duration.between(Instant.now(), target);
        if (remaining.isNegative()) {
            System.out.println("That moment was " + readable(remaining.negated()) + " ago.");
        } else if (remaining.isZero()) {
            System.out.println("That moment is now.");
        } else {
            System.out.println(readable(remaining) + " remaining.");
        }
    }

    private static String readable(Duration duration) {
        long seconds = duration.getSeconds();
        long days = seconds / 86_400;
        long hours = (seconds % 86_400) / 3_600;
        long minutes = (seconds % 3_600) / 60;
        long remainder = seconds % 60;
        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append(days == 1 ? " day " : " days ");
        if (hours > 0 || days > 0) result.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) result.append(minutes).append("m ");
        result.append(remainder).append("s");
        return result.toString().trim();
    }

    private static void zones(String[] parts) {
        requireCount(parts, 1, 2, "zones [region-filter]");
        String filter = parts.length == 2 ? parts[1].toLowerCase(Locale.ROOT) : "";
        List<String> matches = ZoneId.getAvailableZoneIds().stream()
                .filter(id -> id.toLowerCase(Locale.ROOT).contains(filter))
                .sorted()
                .toList();
        if (matches.isEmpty()) {
            System.out.println("No time zones match '" + filter + "'.");
        } else {
            System.out.println(matches.stream().collect(Collectors.joining("  ")));
            System.out.println(matches.size() + " zone(s)");
        }
    }

    private static ZoneId zone(String id) {
        try {
            return ZoneId.of(id);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("unknown time zone '" + id + "'. Use an IANA ID such as Europe/Paris.");
        }
    }

    private static void requireCount(String[] parts, int min, int max, String usage) {
        if (parts.length < min || parts.length > max) {
            throw new IllegalArgumentException("usage: " + usage);
        }
    }

    private static void clear() {
        // ANSI clear sequence works in most modern terminals; fallback is harmless text.
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    private static void printBanner() {
        System.out.println("  CHRONOSHELL 2040");
        System.out.println("  Temporal utilities, straight from the command line.");
        System.out.println("  Type 'help' for commands, or 'exit' to close.\n");
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  now [zone]                         Show the current time (default: system zone)");
        System.out.println("  convert <date-time> <target-zone>  Convert an ISO-8601 time with offset");
        System.out.println("  add <date-time> <duration>         Add an ISO-8601 duration to a date-time");
        System.out.println("  countdown <instant>                Count down to an ISO-8601 instant");
        System.out.println("  zones [filter]                     List time zones, optionally filtered by name");
        System.out.println("  clear                              Clear the terminal screen");
        System.out.println("  about                              About ChronoShell");
        System.out.println("  help, ?                            Show this help");
        System.out.println("  exit, quit                         Close ChronoShell");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  now Asia/Kolkata");
        System.out.println("  convert 2040-06-01T09:30:00-04:00 Europe/Paris");
        System.out.println("  add 2040-01-01T12:00:00Z PT90M");
        System.out.println("  countdown 2040-12-31T23:59:59Z");
    }
}
