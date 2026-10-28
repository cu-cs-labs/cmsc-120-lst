// Pyret's images, for Java: the shapes A1's flags were drawn with, on top of
// JavaFX, the library the reading (Coles 3.7) draws with.
//
// Given: keep this file beside yours and do not edit it. In your own file,
//
//     Img stripe = Img.rectangle(60, 20, "solid", "red");
//     Img flag = Img.above(stripe, Img.rectangle(60, 20, "solid", "white"));
//     Img.show(flag);
//     Check.is(Img.height(flag), 40);
//
// Img.show opens a window with the picture in it; the program ends when the
// last window is closed. Img.width and Img.height give its size, for Check.is.
// Each shape is Pyret's, with the same arguments in the same order, and the
// mode is "solid" or "outline":
//
//     rectangle(width, height, mode, color)    above(top, bottom)
//     square(side, mode, color)                beside(left, right)
//     circle(radius, mode, color)              overlay(front, back)
//     triangle(side, mode, color)              rotate(degrees, image)
//     star(side, mode, color)
//
// above, beside and overlay take exactly two images, as in Pyret, and line
// them up by their middles.
//
// A color is a name, "red" or "darkgreen" or "dark-green", a hex code such as
// "#cc0033", or the reading's Color: Color.RED, Color.color(0.2, 0.4, 0.8).
//
// The reading's own shapes go in too, wherever an Img does: beside, above,
// overlay, rotate, show, width and height all take a Rectangle, a Circle, a
// Polygon, a Pane with shapes in it, and Img.of turns one into an Img:
//
//     Img.beside(new Rectangle(150, 300, Color.RED), Img.circle(30, "solid", "gold"))
//
// A shape is taken as it is at that line: change its color afterwards, and
// the picture made from it stays as it was. Its x and y do not matter here;
// beside, above and overlay decide where it goes.
//
// The reading's own programs are in chap03/ and the folders beside it, and
// Reading.java runs them.
//
// You are welcome to read it. The Rectangle, Circle, Polygon and Color below
// are the reading's; what is new is marked where it is used.

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

class Img {
    // An image is its size, and a way to draw it. The drawing is made fresh
    // each time it is needed, because a JavaFX node can be in one place in a
    // picture only, and Img.beside(s, s) puts the same image in two.
    // Supplier<Node> is a function with no arguments that gives back a Node;
    // `() -> ...` below writes one. Functions as values are week 10.
    private final int width;
    private final int height;
    private final Supplier<Node> draw;

    private Img(int width, int height, Supplier<Node> draw) {
        this.width = width;
        this.height = height;
        this.draw = draw;
    }

    // The size, in pixels, as Pyret's image-width and image-height give it.
    static int width(Img i)  { return i.width; }
    static int height(Img i) { return i.height; }

    // -- the shapes ----------------------------------------------------------
    // Each comes twice: with the reading's Color, and with a color's name,
    // which is turned into a Color and handed to the first.

    static Img rectangle(int width, int height, String mode, Color color) {
        atLeast("width", width, 0);
        atLeast("height", height, 0);
        Paint p = paint(mode, color);
        return new Img(width, height, () -> p.on(new Rectangle(0, 0, width, height)));
    }

    static Img rectangle(int width, int height, String mode, String color) {
        return rectangle(width, height, mode, color(color));
    }

    static Img square(int side, String mode, Color color) {
        return rectangle(side, side, mode, color);
    }

    static Img square(int side, String mode, String color) {
        return square(side, mode, color(color));
    }

    static Img circle(int radius, String mode, Color color) {
        atLeast("radius", radius, 0);
        Paint p = paint(mode, color);
        return new Img(2 * radius, 2 * radius, () -> p.on(new Circle(radius, radius, radius)));
    }

    static Img circle(int radius, String mode, String color) {
        return circle(radius, mode, color(color));
    }

    // Pointing up, all three sides `side` long, so its height is side times
    // the square root of 3, over 2: triangle(30, ...) is 30 wide and 26 high.
    static Img triangle(int side, String mode, Color color) {
        atLeast("side", side, 0);
        Paint p = paint(mode, color);
        double h = side * Math.sqrt(3) / 2;
        return new Img(side, (int) Math.round(h),
                       () -> p.on(new Polygon(0, h, side, h, side / 2.0, 0)));
    }

    static Img triangle(int side, String mode, String color) {
        return triangle(side, mode, color(color));
    }

    // Five points, one at the top, each `side` away from the next, as
    // Pyret's star draws it: star(30, ...) is 49 wide and 46 high.
    static Img star(int side, String mode, Color color) {
        atLeast("side", side, 0);
        Paint p = paint(mode, color);
        // The points sit on a circle of radius r, 72 degrees apart, and the
        // ten corners alternate between it and a smaller circle; these are
        // a regular star's numbers. The loop is week 11, the array week 13.
        double r = side / (2 * Math.sin(Math.toRadians(36)));
        double inner = r * Math.sin(Math.toRadians(18)) / Math.sin(Math.toRadians(54));
        double w = 2 * r * Math.sin(Math.toRadians(72));
        double h = r + r * Math.cos(Math.toRadians(36));
        double[] xy = new double[20];
        for (int k = 0; k < 10; k++) {
            double radius = (k % 2 == 0) ? r : inner;
            double angle = Math.toRadians(-90 + 36 * k);
            xy[2 * k] = w / 2 + radius * Math.cos(angle);
            xy[2 * k + 1] = r + radius * Math.sin(angle);
        }
        return new Img((int) Math.round(w), (int) Math.round(h), () -> p.on(new Polygon(xy)));
    }

    static Img star(int side, String mode, String color) {
        return star(side, mode, color(color));
    }

    // -- anything JavaFX draws ------------------------------------------------

    // A node made the reading's way --- a Rectangle, a Polygon, a Pane with
    // shapes in it, Shape.union of two --- as an Img. What it draws is taken
    // as a picture once, at this line, so changing the node afterwards does
    // not change the Img; its size is the size of what it draws.
    static Img of(Node node) {
        start();
        WritableImage picture = onJavaFX(() -> {
            SnapshotParameters around = new SnapshotParameters();
            around.setFill(Color.TRANSPARENT);
            return node.snapshot(around, null);
        });
        return new Img((int) Math.round(picture.getWidth()), (int) Math.round(picture.getHeight()),
                       () -> new ImageView(picture));
    }

    // -- putting two together ------------------------------------------------

    // Side by side, the first on the left, their middles level.
    static Img beside(Img left, Img right) {
        int w = left.width + right.width;
        int h = Math.max(left.height, right.height);
        return new Img(w, h, () -> group(left, 0, (h - left.height) / 2,
                                         right, left.width, (h - right.height) / 2));
    }

    // One on top of the other, the first on top, their middles in line.
    static Img above(Img top, Img bottom) {
        int w = Math.max(top.width, bottom.width);
        int h = top.height + bottom.height;
        return new Img(w, h, () -> group(top, (w - top.width) / 2, 0,
                                         bottom, (w - bottom.width) / 2, top.height));
    }

    // The first in front of the second, their middles on the same point.
    static Img overlay(Img front, Img back) {
        int w = Math.max(front.width, back.width);
        int h = Math.max(front.height, back.height);
        // The back one is added first, so the front one is drawn over it.
        return new Img(w, h, () -> group(back, (w - back.width) / 2, (h - back.height) / 2,
                                         front, (w - front.width) / 2, (h - front.height) / 2));
    }

    // Turned counterclockwise by `degrees`, as Pyret's rotate turns it. The
    // size is the smallest box the turned picture fits in.
    static Img rotate(double degrees, Img i) {
        double a = Math.toRadians(degrees);
        double cos = Math.abs(Math.cos(a)), sin = Math.abs(Math.sin(a));
        double w = i.width * cos + i.height * sin;
        double h = i.width * sin + i.height * cos;
        return new Img((int) Math.round(w), (int) Math.round(h), () -> {
            Node n = i.draw.get();
            // JavaFX turns clockwise for a positive angle; Pyret the other way.
            n.getTransforms().add(new Rotate(-degrees, i.width / 2.0, i.height / 2.0));
            n.setLayoutX((w - i.width) / 2);
            n.setLayoutY((h - i.height) / 2);
            return new Group(n);
        });
    }

    // -- the reading's shapes, straight in -------------------------------------
    // Each of these turns the shape into an Img with Img.of, then does what
    // the Img version above does. One per mix, because Java picks a method by
    // the types it is handed: two shapes, a shape and an Img, an Img and a shape.

    static Img beside(Node left, Node right) { return beside(of(left), of(right)); }
    static Img beside(Node left, Img right)  { return beside(of(left), right); }
    static Img beside(Img left, Node right)  { return beside(left, of(right)); }

    static Img above(Node top, Node bottom) { return above(of(top), of(bottom)); }
    static Img above(Node top, Img bottom)  { return above(of(top), bottom); }
    static Img above(Img top, Node bottom)  { return above(top, of(bottom)); }

    static Img overlay(Node front, Node back) { return overlay(of(front), of(back)); }
    static Img overlay(Node front, Img back)  { return overlay(of(front), back); }
    static Img overlay(Img front, Node back)  { return overlay(front, of(back)); }

    static Img rotate(double degrees, Node n) { return rotate(degrees, of(n)); }
    static void show(Node n)                  { show(of(n)); }
    static int width(Node n)                  { return width(of(n)); }
    static int height(Node n)                 { return height(of(n)); }

    // -- showing one ---------------------------------------------------------

    // A window with the picture in it, on white. Every call opens a window
    // of its own; the program ends when the last one is closed.
    static void show(Img i) {
        start();
        // JavaFX draws on a thread of its own, its "application thread", and
        // a window may be made only there: runLater hands it the work.
        Platform.runLater(() -> {
            Pane root = new Pane(i.draw.get());
            Stage stage = new Stage();
            stage.setTitle("Img, " + i.width + " by " + i.height);
            stage.setScene(new Scene(root, Math.max(i.width, 1), Math.max(i.height, 1), Color.WHITE));
            stage.show();
        });
    }

    // -- the parts nobody calls ----------------------------------------------

    private static boolean started = false;

    // JavaFX logs a warning, in red, that it was loaded as a plain library
    // rather than as a module. That is true and harmless here, so its log is
    // switched off. Kept in a field, because a log nobody holds is forgotten
    // and comes back on.
    private static final java.util.logging.Logger javafxLog = java.util.logging.Logger.getLogger("javafx");

    // JavaFX starts once per program. The reading's programs start it with
    // launch(), from a class that extends Application; this does the same
    // without one. JavaFX then keeps the program running until its last
    // window closes, so a program that opened none would never end: a
    // watcher waits for main to finish, and stops JavaFX if no window is open.
    private static void start() {
        if (started) {
            return;
        }
        started = true;
        javafxLog.setLevel(java.util.logging.Level.OFF);
        Platform.startup(() -> {});
        Thread main = Thread.currentThread();
        Thread watcher = new Thread(() -> {
            try {
                main.join();
            } catch (InterruptedException e) {
                return;
            }
            Platform.runLater(() -> {
                if (Window.getWindows().isEmpty()) {
                    Platform.exit();
                }
            });
        });
        watcher.setDaemon(true);
        watcher.start();
    }

    // Work done on JavaFX's thread, waited for here, and its result handed
    // back: taking a picture of a node is work JavaFX does only there.
    private static <T> T onJavaFX(Callable<T> work) {
        FutureTask<T> task = new FutureTask<>(work);
        Platform.runLater(task);
        try {
            return task.get();
        } catch (Exception e) {
            throw new IllegalStateException("JavaFX could not draw it: " + e.getCause(), e);
        }
    }

    // Two images in one group, each moved to where its top left corner goes.
    // The places are whole pixels: on a half pixel, the edge between two
    // stripes can be drawn as a faint line.
    private static Node group(Img a, int ax, int ay, Img b, int bx, int by) {
        Node na = a.draw.get();
        Node nb = b.draw.get();
        na.setLayoutX(ax);
        na.setLayoutY(ay);
        nb.setLayoutX(bx);
        nb.setLayoutY(by);
        return new Group(na, nb);
    }

    // The mode and the color, checked once, when the shape is made: a typo in
    // either stops the program at the line that has it, not at show.
    private record Paint(boolean solid, Color color) {
        Shape on(Shape s) {
            if (solid) {
                s.setFill(color);
            } else {
                s.setFill(null);
                s.setStroke(color);
                s.setStrokeType(StrokeType.INSIDE);
            }
            return s;
        }
    }

    private static Paint paint(String mode, Color color) {
        if (!mode.equals("solid") && !mode.equals("outline")) {
            throw new IllegalArgumentException("the mode is \"solid\" or \"outline\", not \"" + mode + "\"");
        }
        return new Paint(mode.equals("solid"), color);
    }

    private static Color color(String name) {
        try {
            return Color.web(name.replace("-", ""));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("there is no color \"" + name + "\"");
        }
    }

    private static void atLeast(String what, int value, int least) {
        if (value < least) {
            throw new IllegalArgumentException("the " + what + " is " + value + "; it cannot be below " + least);
        }
    }
}
