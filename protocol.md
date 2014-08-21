Oppo control protocol details
=============================

- Channel to device: HTTP server on port 436, message format application/json
- Channel from device: HTTP server on port 4360, message format application/json
- All requests are GET requests, all payloads are JSON
- Messages are sent as single-value GET request parameters; responses in response body
    - 'commands' are what we call messages to device; 'events' are messages back from the device
    - 'responses' are replies to either commands or events
- All device responses contain success:true (presumably unless something goes wrong)
- Most (not all) device responses contain msg:"blah" (often blank, usually human readable)
- All events seem to include quick_start, target_addr, source_addr
- Keepalive supported on device server
- Device responses include content length, cache-blowing headers


Command and event script (in capture order)
-------------------------------------------

* Command *Unknown endpoint*
    * Request: *Any request*
    * Response: `{"success":true,"source":"192.168.1.2:2455 /","msg":"unknown request."}`
    * Error response from any unknown endpoint (I think)
    
* Command `getmainfirmwareversion`
    * Request: *none*
    * Response: `{"success":true,"bbkver":"BDP10X-75-0515"}`
    * *50ms* Device info block. Don't need to be signed in to make this call

* Command `signin`
    * `{"appIconType":1}`
    * `{"success":true,"msg":"","player_name":"BDP-103_OPPO BDP-103","player_port":"436"}`
    * *9ms* This seems to be some kind of handshake scenario that perhaps sets us up to receive messages back from the remote

* Command `getdevicelist`
    * Request: *none*
    * Response: `{"success":true,"msg":"","devicelist":[{"sub_type":"disc","customName":"DVD Video","path":"/mnt/disc_00_0","serial_number":"","logic_part_idx":"0","total_size_bytes":"0","avail_size_bytes":"0"},{"sub_type":"cifs","customName":"GROUNDED","path":"GROUNDED","serial_number":"","logic_part_idx":"0","total_size_bytes":"0","avail_size_bytes":"0"},{"sub_type":"cifs","customName":"SUMOMO","path":"SUMOMO","serial_number":"","logic_part_idx":"0","total_size_bytes":"0","avail_size_bytes":"0"}]}`
    * *8ms* Oppo seems to cache devices even after they're long gone

* Command `getglobalinfo`
    * Request: *none*
    * Response: `{"success":true,"curr_volume":32,"min_volume":0,"max_volume":100,"is_muted":false,"cur_media_type":5,"is_audio_playing":false,"is_pic_playing":false,"is_video_playing":false,"is_bdmv_playing":false,"is_disc_playing":false,"activeapp":"SETUP","msg":""}`
    * *19ms* Various info about the current state of the player
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"NOTIFY_PLAYER_ALIVE","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
	* Response: `{"success":true,"msg":"do nothing."}`
	* Keepalive, delivered every 5 seconds, even when device is off I think?
	
* Event `sendPushMsg`
    * Request: `{"msg_type":"PLAYER_POWER_ON","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * Arrives right after NOTIFY_PLAYER_ALIVE?  Or did I hit the button?  Dunno?
    
* Command `sendremotekey`
    * Request: `{"key":<code>}`
    * Response: `{"success":true,"msg":""}`
    * This represents the user pressing a key on the remote (see below for key reference)
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"AM_NOTIFY_APP_ACTIVE_misc","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * 'misc' mode is active?
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"AM_NOTIFY_APP_PAUSED_misc","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * 'misc' mode is paused?
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"AM_NOTIFY_APP_ACTIVE_mediac","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * media centre mode is active?
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"DM_LDR_EVT_DISC_LOADING","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * Tells me the device is loading a disc?

* Event `sendPushMsg`
    * Request: `{"msg_type":"DEV_MOUNTED","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * Tells me the device finished loading a disc?
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"MISC_NY_DISC_TYPE_MSG","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * It's like a message telling me what kind of disc was loaded, sans the part where it tells me what type of disc it was.
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"PLAYER_PRE_POWER_OFF","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * Power is starting to go off (linked to delivery of POW keypress)
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"MC_APP_PAUSE_FCT","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * Media centre pause indicator?  Confirmation of button press or something?
    
* Event `sendPushMsg`
    * Request: `{"msg_type":"AM_NOTIFY_APP_PAUSED_mediac","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}`
    * Response: `{"success":true,"msg":"do nothing."}`
    * Media center is paused?
    
* Command `getplayingappname`
    * Request: *none*
    * Response: `{"success":true,"source":"192.168.1.3:52975 /getplayingappname","msg":"unknown request."}`
    * Perhaps this is the client trying to talk to a different device
    
* Command `signout`
    * Request: `{"appIconType":1}`
    * Response: (doesn't even reply)
    * Sign out (unregister for notifications perhaps?)


Key code list:
    * `NRT` (?)
    * `NUx` (number `x`)
    * `POW` (power)
    * `EJT` (eject)
