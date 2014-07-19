package com.lantopia.oppo;

/**
 * @author Mark McKenna &lt;mark.denis.mckenna@gmail.com&gt;
 * @version 0.1
 * @since 15/07/2014
 *
 * Represents an Oppo device to a user of the library.  This class' public methods expose an interface that
 * clients will recognize as appropriate for talking to an Oppo device, with all the various arguments that seem
 * to make sense at that level.
 */
public interface Device {
    enum Power { ON, OFF, STANDBY }

    // TODO: Add public accessors here to streamline communication with the device.
    // Remember that ultimately, we want a tool that can synchronize control actions across several devices and possibly
    // other apps--so we should structure this in a way that makes building 'action chains' that span devices
    // a possibility.
    // Also remember that most of the values stored here are collected asynchronously, and many of them won't be
    // available until several seconds after the device is powered on and some chatter happens.  We want to make those
    // values cached locally and accessible quickly; but we also want to make sure if someone asks for one of those
    // values, they don't get garbage instead.
    // Various approaches:
    // * Lock the device down using states; collect information during state transition and expose fields when in the
    //   correct state.  Make the fields be always local and populated when present.  This leaves us at risk of
    //   screwing up our states and failing to deliver content.
    // * Wrap properties in synchronization wrappers so requests can be lazily dispatched to the remote server.  Fetch
    //   eagerly when suitable, but offer no guarantee to the client app as to when the properties will become available.
    //   Make the properties available in blocking and non-blocking (and time-limited blocking) flavours.  This leaves us
    //   at risk of accidentally blocking the app forever.
    // * Expose properties as getters with callbacks; property requests respond by calling the callback function.  No
    //   guarantee about if/when the callback will come back, which might result in some unexpected callbacks getting
    //   fired way down the road (e.g. one leftover from a previous session that finally gets called much later on,
    //   perhaps causing a surprise device state change or something)
    // * Expose an event bus, notifying clients of population of and changes to property values.  Implies that the
    //   client would generally listen statically for various events, but the client might also have to manage
    //   synthetic addition/removal of listeners, which would revert back to option #3 in terms of what problems it
    //   could cause.  Event systems are also non-self-documenting because they aren't Javadoc'd.
}
