LibOppo:  Oppo BDP-103 device control library
=============================================

Layering Concepts
-----------------

* At the bottom we have a service that listens for HTTP GET requests on a fixed port and responds to them; and that
  pushes out events to an HTTP server on the machine that advertises itself.  The messages both in and out are JSON.
  - Does it support multicast event deliveries or does one client supersede a prior one?
  - Do we have any control over the port it sends back to?

* Above that, we have a 'bus' that lets you send and receive JSON-format messages to/from the device.  This should be 
  accessible from the upstream code, in case newer Oppo devices support additional commands/events that we weren't 
  anticipating and clients want to add support for those.  The bus should use the service above to communicate with
  the device, and expose the results of that communication as JSON payloads.  It should also let the sender determine
  the endpoint that will receive the message, within the fixed scope of the host/port combination.
  - We should communicate using String here, so we aren't forced to choose between parser options such as JsonNode,
    ObjectMapper or JsonParser.  It would make sense for the layer below here to use ObjectMapper, but it would
    probably also make sense to let clients use JsonNode if they need to extend the protocol.
  - There are two modes of response here--responses to HTTP requests, and unrequested inbound messages.  Sent actions
    should have a callback that provides the HTTP response payload; whereas received events will themselves expect
    to receive an HTTP response back from the client, for delivery back to the device.
  
* Above *that* bus, we should have a library of known commands that can be delivered, and events that can be received.
  This gives client integrators a really quick way to assemble payloads for use with the bus, above--just assemble the
  command object and dispatch it.  This layer should also include a bus wrapper that translates these command objects
  into JSON strings, and translates received JSON into known event objects.
  - This constitutes an 'action-based API' where users can straightforwardly dispatch actions to the device and see them
    happen.  These actions should be able to respond with the proper feedback to the remote,
    populate boilerplate fields, and properly accept inbound events by sending the appropriate response where some kind
    of user action isn't required to go back out (hopefully this is never needed).
    
* We know we're going to want to use the library to automate actions across several devices at once.  There should be
  some classes that can be used to turn the commands above into a script, where the same script could contain commands
  for multiple devices.  This could be accomplished through wrappers, but it implies that the commands in the API above
  should be separate objects and not just methods on a class.
    
* Should there be another interface here, that wraps up the device as a stateful object with simple calls on it that
  can be used to make things happen?  This would give a spot to cache data received from the device and locally
  represent the device's state.  This would also add a bunch of complexity because of the risk of the device's state 
  going out of sync.  It might not add much value there.  This should probably be considered a later phase development.