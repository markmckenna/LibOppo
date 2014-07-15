Oppo control protocol details
=============================

- Channel to device: HTTP server on port 436, message format application/json
- Channel from device: HTTP server on port 4360, message format application/json

Command index:
	GET /
		{"success":true,"source":"192.168.1.2:2455 /","msg":"unknown request."}

	GET /signin?{"appIconType":1}
		{"success":true,"msg":"","player_name":"BDP-103_OPPO BDP-103","player_port":"436"}

	GET /getdevicelist
		{"success":true,"msg":"","devicelist":[{"sub_type":"disc","name":"DVD Video","path":"/mnt/disc_00_0","serial_number":"","logic_part_idx":"0","total_size_bytes":"0","avail_size_bytes":"0"},{"sub_type":"cifs","name":"GROUNDED","path":"GROUNDED","serial_number":"","logic_part_idx":"0","total_size_bytes":"0","avail_size_bytes":"0"},{"sub_type":"cifs","name":"SUMOMO","path":"SUMOMO","serial_number":"","logic_part_idx":"0","total_size_bytes":"0","avail_size_bytes":"0"}]}

	GET /getglobalinfo
		{"success":true,"curr_volume":32,"min_volume":0,"max_volume":100,"is_muted":false,"cur_media_type":5,"is_audio_playing":false,"is_pic_playing":false,"is_video_playing":false,"is_bdmv_playing":false,"is_disc_playing":false,"activeapp":"SETUP","msg":""}

	GET /sendremotekey?%7B%22key%22%3A%22NRT%22%7D
		{"success":true,"msg":""}

Event index:
	GET /sendPushMsg?{"msg_type":"NOTIFY_PLAYER_ALIVE","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}
		{"success":true,"msg":"do nothing."}

	GET /sendPushMsg?{"msg_type":"PLAYER_POWER_ON","quick_start":true,"target_addr":"192.168.1.3","source_addr":"192.168.1.106"}
		{"success":true,"msg":"do nothing."}
