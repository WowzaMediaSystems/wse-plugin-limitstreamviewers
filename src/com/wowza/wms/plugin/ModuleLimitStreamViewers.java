/*
 * This code and all components (c) Copyright 2006 - 2016, Wowza Media Systems, LLC. All rights reserved.
 * This code is licensed pursuant to the Wowza Public License version 1.0, available at www.wowza.com/legal.
 */
package com.wowza.wms.plugin;

import java.util.HashMap;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.ApplicationInstance;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.logging.WMSLoggerIDs;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.rtp.model.RTPSession;
import com.wowza.wms.rtp.model.RTPUrl;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.MediaStream;
import com.wowza.wms.stream.mediacaster.MediaStreamMediaCasterUtils;
import com.wowza.wms.util.ModuleUtils;

public class ModuleLimitStreamViewers extends ModuleBase
{
	public static final String MODULE_NAME = "ModuleLimitStreamViewers";
	public static final String PROP_NAME_PREFIX = "limitStreamViewers";
	public static final int MAXVIEWERS = 200;

	WMSLogger logger = null;
	private IApplicationInstance appInstance;

	private HashMap<String, Integer> streamLimits = new HashMap<String, Integer>();

	private int maxStreamViewers = MAXVIEWERS;
	private boolean logConnectionCounts = true;
	private boolean logRejections = true;

	public void onAppStart(IApplicationInstance appInstance)
	{
		logger = WMSLoggerFactory.getLoggerObj(appInstance);
		this.appInstance = appInstance;
		// old property name
		this.maxStreamViewers = appInstance.getProperties().getPropertyInt("maxStreamViewers", MAXVIEWERS);
		// new property name
		this.maxStreamViewers = appInstance.getProperties().getPropertyInt(PROP_NAME_PREFIX + "MaxViewers", MAXVIEWERS);

		this.logConnectionCounts = appInstance.getProperties().getPropertyBoolean(PROP_NAME_PREFIX + "LogConnectionCounts", this.logConnectionCounts);
		this.logRejections = appInstance.getProperties().getPropertyBoolean(PROP_NAME_PREFIX + "LogRejections", this.logRejections);
		if(logger.isDebugEnabled())
		{
			this.logConnectionCounts = true;
			this.logRejections = true;
		}
		
		fillStreamLimits();

		logger.info(MODULE_NAME + " limit: " + this.maxStreamViewers + " logConnections: " + this.logConnectionCounts + ", logRejections: " + this.logRejections, WMSLoggerIDs.CAT_application, WMSLoggerIDs.EVT_comment);
	}

	public void play(IClient client, RequestFunction function, AMFDataList params)
	{
		String streamName = params.getString(PARAM1);

		//get the real stream name if this is an alias.
		streamName = ((ApplicationInstance)appInstance).internalResolvePlayAlias(streamName, client);

		int count = getViewerCounts(streamName, client);
		int limit = getStreamLimit(streamName);
		if (count < limit)
		{
			this.invokePrevious(client, function, params);
		}
		else
		{
			IMediaStream stream = getStream(client, function);
			if (stream != null)
			{
				String code = "NetStream.Play.Failed";
				String description = MODULE_NAME + ": Over viewer limit[" + limit + "]";

				sendStreamOnStatusError(stream, code, description);

				if (logRejections)
					logger.info(MODULE_NAME + "ModuleLimitViewers: Over viewer limit[" + limit + " streamName: " + streamName + "]", WMSLoggerIDs.CAT_application, WMSLoggerIDs.EVT_comment);
			}
		}
	}

	public void onHTTPSessionCreate(IHTTPStreamerSession httpSession)
	{
		String streamName = httpSession.getStreamName();
		// No need to get the alias here as it has already been done so just need to get the connection count.
		int count = getViewerCounts(streamName);
		int limit = getStreamLimit(streamName);
		// When an HTTP session is created, the stream is already allocated here so will register as one http count.
		if (count > limit)
		{
			httpSession.rejectSession();
			if (logRejections)
				logger.info(MODULE_NAME + ": Over viewer limit[" + limit + " streamName: " + streamName + "]", WMSLoggerIDs.CAT_application, WMSLoggerIDs.EVT_comment);
		}
	}

	public void onRTPSessionCreate(RTPSession rtpSession)
	{
		String uri = rtpSession.getUri();
		RTPUrl url = new RTPUrl(uri);
		String streamName = url.getStreamName();

		streamName = ((ApplicationInstance)appInstance).internalResolvePlayAlias(streamName, rtpSession);

		int count = getViewerCounts(streamName);
		int limit = getStreamLimit(streamName);
		if (count >= limit)
		{
			rtpSession.rejectSession();
			if (logRejections)
				logger.info(MODULE_NAME + ": Over viewer limit[" + limit + " streamName: " + streamName + "]", WMSLoggerIDs.CAT_application, WMSLoggerIDs.EVT_comment);
		}
	}

	private void fillStreamLimits()
	{
		String streamLimit = appInstance.getProperties().getPropertyStr(PROP_NAME_PREFIX + "ByList", null);
		if(streamLimit != null)
		{
			String[] streams = streamLimit.split(",");
			if (streams.length > 0)
			{
				for (int i = 0; i < streams.length; i++)
				{
					String[] streamEqValue = streams[i].split("=");
					if (streamEqValue.length == 2)
					{
						String stream = streamEqValue[0];
						try
						{
							int value = Integer.parseInt(streamEqValue[1]);
							this.streamLimits.put(stream, new Integer(value));
							logger.info(MODULE_NAME + " Limits: " + stream + " = " + value);
						}
						catch (NumberFormatException e)
						{
							logger.warn(MODULE_NAME + " limit not set for stream. NumberFormatException: " + streams[i]);
						}
					}
				}
			}
		}
	}

	private int getStreamLimit(String streamName)
	{
		streamName = decodeStreamName(streamName);
		if (this.streamLimits.containsKey(streamName))
		{
			return this.streamLimits.get(streamName);
		}
		return this.maxStreamViewers;
	}

	private String decodeStreamName(String streamName)
	{
		String streamExt = MediaStream.BASE_STREAM_EXT;
		if (streamName != null)
		{
			String[] streamDecode = ModuleUtils.decodeStreamExtension(streamName, streamExt);
			streamName = streamDecode[0];
			streamExt = streamDecode[1];

			boolean isStreamNameURL = streamName.indexOf("://") >= 0;
			int streamQueryIdx = streamName.indexOf("?");
			if (!isStreamNameURL && streamQueryIdx >= 0)
			{
				streamName = streamName.substring(0, streamQueryIdx);
			}
		}
		return streamName;
	}

	private int getViewerCounts(String streamName)
	{
		return getViewerCounts(streamName, null);
	}

	private synchronized int getViewerCounts(String streamName, IClient client)
	{
		int count = 0;
		int rtmpCount = 0;
		int httpCount = 0;
		int rtpCount = 0;

		streamName = decodeStreamName(streamName);
		if (streamName != null)
		{
			rtmpCount += appInstance.getPlayStreamCount(streamName);
			httpCount += appInstance.getHTTPStreamerSessionCount(streamName);
			rtpCount += appInstance.getRTPSessionCount(streamName);

			// Test for mediaCaster streams like wowz://[origin-ip]:1935/origin/myStream.
			String mediaCasterName = MediaStreamMediaCasterUtils.mapMediaCasterName(appInstance, client, streamName);
			if (!mediaCasterName.equals(streamName))
			{
				if (logConnectionCounts)
					logger.info(MODULE_NAME + ".getViewerCounts matching mediaCaster name: " + mediaCasterName, WMSLoggerIDs.CAT_application, WMSLoggerIDs.EVT_comment);
				rtmpCount += appInstance.getPlayStreamCount(mediaCasterName);
				httpCount += appInstance.getHTTPStreamerSessionCount(mediaCasterName);
				rtpCount += appInstance.getRTPSessionCount(mediaCasterName);
			}
			count = rtmpCount + httpCount + rtpCount;

			if (logConnectionCounts)
				logger.info(MODULE_NAME + ".getViewerCounts streamName: " + streamName + " total:" + count + " rtmp: " + rtmpCount + " http: " + httpCount + " rtp: " + rtpCount, WMSLoggerIDs.CAT_application, WMSLoggerIDs.EVT_comment);

		}
		return count;
	}
}
