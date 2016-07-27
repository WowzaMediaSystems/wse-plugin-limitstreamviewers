# LimitStreamViewers
The **ModuleLimitStreamViewers** module for [Wowza Streaming Engine™ media server software](https://www.wowza.com/products/streaming-engine) enables you to limit the total number of concurrent clients by stream.

## Prerequisites
Wowza Streaming Engine 4.0.0 (or later) or Wowza Media Server™ 3.6.3 (or later) is required.

## Usage
This module provides the following functionality:

* Specify a limit of concurrent clients at the stream level.
* Specify different limits across multiple streams.  
* Set client limits for RTMP, RTSP, and HTTP playback protocols.

The stream won't play for any viewers above the concurrent users limit. What these viewers see will vary depending on the behavior of their player.

## More resources
[Wowza Streaming Engine Server-Side API Reference](https://www.wowza.com/resources/WowzaStreamingEngine_ServerSideAPI.pdf)

[How to extend Wowza Streaming Engine using the Wowza IDE](https://www.wowza.com/forums/content.php?759-How-to-extend-Wowza-Streaming-Engine-using-the-Wowza-IDE)

Wowza Media Systems™ provides developers with a platform to create streaming applications and solutions. See [Wowza Developer Tools](https://www.wowza.com/resources/developers) to learn more about our APIs and SDK.

To use the compiled version of this module, see [How to limit the number of viewers of a stream (LimitStreamViewers)](https://www.wowza.com/forums/content.php?148-How-to-limit-the-number-of-viewers-to-a-stream-%28ModuleLimitStreamViewers%29).

## Contact
[Wowza Media Systems, LLC](https://www.wowza.com/contact)

## License
This code is distributed under the [Wowza Public License](https://github.com/WowzaMediaSystems/wse-plugin-limitstreamviewers/blob/master/LICENSE.txt).

![alt tag](http://wowzalogs.com/stats/githubimage.php?plugin=wse-plugin-limitstreamviewers)
