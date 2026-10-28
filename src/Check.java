// Pyret's `is`, for Java.
//
// Given: keep this file beside yours and do not edit it. In main, write
//
//     Check.is(stripeWidth(10), 60);
//
// once per example, and Check.done() as the last line. A check that fails
// prints what it got and what it expected; done() prints the count.
//
// You are welcome to read it. Everything in here is Java you have seen or
// will see within a few weeks, and where it is the latter, a comment says so.

// A class is a box that methods live in. `Check` is the name you type before
// the dot; what a class is for, properly, is week 6.
class Check {
    // Two counts that go up as the checks run. `static` means there is one
    // of each for the whole program, not one per anything. Week 6 as well.
    static int passed = 0;
    static int failed = 0;
    static boolean finished = false;

    // A hook: code Java runs as the program ends, whether main finished or
    // stopped on an error. If done() never ran, it says so, with the count of
    // the checks that did: a main that stopped early has checks it never
    // reached, so it never says "Looks shipshape". `() -> { ... }` is a
    // function written in place, which is week 10.
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!finished) {
                IO.println("Check.done() never ran: " + passed + " passed, " + failed
                           + " failed before the program ended");
            }
        }));
    }

    // Four `is` here and two below, all with the same name, one per kind of
    // value. Java picks the one whose types match what you pass: the first
    // for stripeWidth(10) and 60, the fourth for flagRow(1) and text.
    // `static` on a method is what lets you write Check.is(...) without
    // making a Check first.
    //
    // Two numbers are equal when == says so. Two strings are equal when
    // .equals says so, and why those are two different questions is next
    // class.
    static void is(int got, int expected)         { record(got == expected, "" + got, "" + expected); }
    static void is(double got, double expected)   { record(got == expected, "" + got, "" + expected); }
    static void is(boolean got, boolean expected) { record(got == expected, "" + got, "" + expected); }
    static void is(String got, String expected)   { record(got.equals(expected), got, expected); }

    // Two more, for the types you make yourself: an enum, and a record. Both
    // ask .equals, which on a record compares the parts, one by one:
    //
    //     Check.is(sizeFor(12), Size.SMALL);
    //     Check.is(upsideDown(f), new Flag(..., ..., ...));
    //
    // <E extends Enum<E>> says the two values are of the same enum, and
    // Record is every record; neither is something this course writes.
    // There is no `is` for any two values whatever: Check.is(600, "600")
    // would then run and print got 600, expected 600.
    static <E extends Enum<E>> void is(E got, E expected) { record(got.equals(expected), "" + got, "" + expected); }
    static void is(Record got, Record expected)           { record(got.equals(expected), "" + got, "" + expected); }

    // And one for the lists of Lists.java, which asks .equals too: the same
    // elements, in the same order. Check.is(Lists.filter(h -> h > 8, hours),
    // List.of(9, 11)). Data is the name those lists answer to here, so that
    // Check.java works with or without Lists.java beside it.
    interface Data {}
    static void is(Data got, Data expected) {
        record(got.equals(expected), "" + got, "" + expected);
    }

    // Every `is` ends up here with the verdict and both values as text, so a
    // failure prints the same way whatever the type was. `"" + got` is how a
    // number becomes text: + joins, and joins a number onto text without
    // being asked.
    static void record(boolean ok, String got, String expected) {
        if (ok) {
            passed = passed + 1;
        } else {
            failed = failed + 1;
            IO.println("FAIL check " + (passed + failed) + ": got " + got + ", expected " + expected);
        }
    }

    // The last line of main: Pyret's "Looks shipshape", or the two counts.
    // With no checks at all, it says so: nothing failed, and nothing passed.
    static void done() {
        finished = true;
        if (passed + failed == 0) {
            IO.println("No checks yet: put a Check.is line in main for each example");
        } else if (failed == 0) {
            IO.println("Looks shipshape, all " + passed + " checks passed");
        } else {
            IO.println(passed + " passed, " + failed + " FAILED");
        }
    }
}
