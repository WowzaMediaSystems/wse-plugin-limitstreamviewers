# LimitStreamViewers
The **ModuleLimitStreamViewers** module for [Wowza Streaming Engine™ media server software](https://www.wowza.com/products/streaming-engine) enables you to limit the total number of concurrent clients by stream.

This repo includes a [compiled version](/lib/wse-plugin-limitstreamviewers.jar).

## Prerequisites
Wowza Streaming Engine 4.0.0 (or later) or Wowza Media Server™ 3.6.3 (or later) is required.

## Usage
This module provides the following functionality:

* Specify a limit of concurrent clients at the stream level.
* Specify different limits across multiple streams.  
* Set client limits for RTMP, RTSP, and HTTP playback protocols.

The stream won't play for any viewers above the concurrent users limit. What these viewers see will vary depending on the behavior of their player.

## More resources
To use the compiled version of this module, see [Limit the number of viewers of a stream with a Wowza Streaming Engine Java module](https://www.wowza.com/docs/how-to-limit-the-number-of-viewers-to-a-stream-modulelimitstreamviewers).

[Wowza Streaming Engine Server-Side API Reference](https://www.wowza.com/resources/serverapi/)

[How to extend Wowza Streaming Engine using the Wowza IDE](https://www.wowza.com/docs/how-to-extend-wowza-streaming-engine-using-the-wowza-ide)

Wowza Media Systems™ provides developers with a platform to create streaming applications and solutions. See [Wowza Developer Tools](https://www.wowza.com/developers) to learn more about our APIs and SDK.

## Contact
[Wowza Media Systems, LLC](https://www.wowza.com/contact)

## License
This code is distributed under the [Wowza Public License](/LICENSE.txt).
