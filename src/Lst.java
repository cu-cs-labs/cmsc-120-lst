// Pyret's List, in Java: a list is empty, or a first element linked to the
// rest of the list. Keep this file beside yours and do not edit it.
//
//     Lst<Integer> units = Lst.of(4, 10, 20);    // [list: 4, 10, 20]
//
//     Lst.filter(u -> u >= 10, units)            // filter(lam(u): u >= 10 end, units)
//     Lst.map(u -> u * 6, units)                 // map(lam(u): u * 6 end, units)
//     Lst.fold((sum, u) -> sum + u, 0, units)    // fold(lam(sum, u): sum + u end, 0, units)
//
// and Pyret's cases, as a switch with a case for each kind of list:
//
//     return switch (units) {
//         case Lst.Empty() -> 0;                 // | empty => 0
//         case Lst.Link(var u, var rest) -> ...; // | link(u, rest) => ...
//     };
//
// You are welcome to read it: each function below is that switch, the way
// week 3 wrote filter, map and fold by hand.
//
// `sealed ... permits` says a list is an Empty or a Link and nothing else,
// which is how Java knows a switch with those two cases is complete. <T> is
// the kind of element: Lst<Integer>, Lst<String>, Lst<Img>.
sealed interface Lst<T> extends Check.Data permits Lst.Empty, Lst.Link {

    // The empty list, Pyret's empty. It prints the way Pyret prints it.
    record Empty<T>() implements Lst<T> {
        public String toString() {
            return "[list: ]";
        }
    }

    // A first element linked to the rest, Pyret's link(first, rest).
    record Link<T>(T first, Lst<T> rest) implements Lst<T> {
        public String toString() {
            return "[list: " + joined(this) + "]";
        }
    }

    // [list: ...]: the elements given, in order. `T... xs` takes any number
    // of them, numbered from 0.
    @SafeVarargs
    static <T> Lst<T> of(T... xs) {
        return from(xs, 0);
    }

    // the elements keep says yes to, in order
    static <T> Lst<T> filter(java.util.function.Predicate<T> keep, Lst<T> xs) {
        return switch (xs) {
            case Empty<T>() -> new Empty<>();
            case Link<T>(var first, var rest) ->
                keep.test(first) ? new Link<>(first, filter(keep, rest)) : filter(keep, rest);
        };
    }

    // f of each element, in order
    static <T, R> Lst<R> map(java.util.function.Function<T, R> f, Lst<T> xs) {
        return switch (xs) {
            case Empty<T>() -> new Empty<>();
            case Link<T>(var first, var rest) -> new Link<>(f.apply(first), map(f, rest));
        };
    }

    // the elements combined from the front: combine the value so far with
    // the first element, then go on with the rest; base for the empty list
    static <T, R> R fold(java.util.function.BiFunction<R, T, R> combine, R base, Lst<T> xs) {
        return switch (xs) {
            case Empty<T>() -> base;
            case Link<T>(var first, var rest) -> fold(combine, combine.apply(base, first), rest);
        };
    }

    // how many elements
    static <T> int length(Lst<T> xs) {
        return switch (xs) {
            case Empty<T>() -> 0;
            case Link<T>(var first, var rest) -> 1 + length(rest);
        };
    }

    // the elements from position i on, linked in order
    private static <T> Lst<T> from(T[] xs, int i) {
        if (i == xs.length) {
            return new Empty<>();
        } else {
            return new Link<>(xs[i], from(xs, i + 1));
        }
    }

    // the elements as text, a comma and a space between them, for printing
    private static <T> String joined(Lst<T> xs) {
        return switch (xs) {
            case Empty<T>() -> "";
            case Link<T>(var first, Empty<T> rest) -> "" + first;
            case Link<T>(var first, var rest) -> first + ", " + joined(rest);
        };
    }
}
