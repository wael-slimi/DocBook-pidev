package org.docbook.mains;

public class Launcher {
    public static void main(String[] args) {
        // Calling mainFx.main here tricks the JVM
        // into bypass the "Runtime Components" check.cd
        mainFx.main(args);
    }
}
