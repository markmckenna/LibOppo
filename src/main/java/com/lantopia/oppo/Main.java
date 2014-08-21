package com.lantopia.oppo;

import com.lantopia.libjava.log.Logger;
import com.lantopia.libjava.log.TextLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.PrintWriter;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public final class Main {
    private static final Logger logger = TextLogger.make(new PrintWriter(System.out));

    private Main() {}

    public static Logger logger() { return logger; }
}
