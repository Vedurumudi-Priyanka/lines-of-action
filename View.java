package loa;

/** An updateable view of a LOA board. */

interface View {

    /** Update the current view according to the game on CONTROLLER. */
    void update(Game controller);

}
