package loa;

/** An object that reports errors and other notifications to the user.

 */
interface Reporter {

    /** Report an error as indicated by FORMAT and ARGS, which have
     *  the same meaning as in String.format. */
    void reportError(String format, Object... args);

    /** Display a message indicated by FORMAT and ARGS, which have
     *  the same meaning as in String.format. */
    void reportNote(String format, Object... args);

    /** Display MOVE as needed. */
    void reportMove(Move move);

}
