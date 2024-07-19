package com.doge.torrent.announce.model;

public class AnnounceRequestBuilder {
	private String trackerAnnounceUrl;
	private String infoHash;
	private String peerId;
	private Long downloaded;
	private Long uploaded;
	private Long left;
	private Integer port;
	private Boolean compact;
	private Event event;

	private AnnounceRequestBuilder(String trackerAnnounceUrl) {
		this.trackerAnnounceUrl = trackerAnnounceUrl;
		downloaded = 0L;
		uploaded = 0L;
		compact = true;
		event = Event.NONE;
	}

	public static AnnounceRequestBuilder fromAnnouncementRequest(AnnounceRequest request) {
		return new AnnounceRequestBuilder(request.trackerAnnounceUrl())
				.infoHash(request.infoHash())
				.peerId(request.peerId())
				.downloaded(request.downloaded())
				.uploaded(request.uploaded())
				.left(request.left())
				.compact(request.compact())
				.event(request.event());
	}

	public static AnnounceRequestBuilder fromUrl(String trackerAnnounceUrl) {
		return new AnnounceRequestBuilder(trackerAnnounceUrl);
	}

	public AnnounceRequestBuilder infoHash(String infoHash) {
		this.infoHash = infoHash;
		return this;
	}

	public AnnounceRequestBuilder peerId(String peerId) {
		this.peerId = peerId;
		return this;
	}

	public AnnounceRequestBuilder downloaded(Long downloaded) {
		this.downloaded = downloaded;
		return this;
	}

	public AnnounceRequestBuilder uploaded(Long uploaded) {
		this.uploaded = uploaded;
		return this;
	}

	public AnnounceRequestBuilder left(Long left) {
		this.left = left;
		return this;
	}

	public AnnounceRequestBuilder compact(Boolean compact) {
		this.compact = compact;
		return this;
	}

	public AnnounceRequestBuilder event(Event event) {
		this.event = event;
		return this;
	}

	public AnnounceRequestBuilder port(Integer port) {
		this.port = port;
		return this;
	}

	public AnnounceRequest build() {
		return new AnnounceRequest(trackerAnnounceUrl,
								   infoHash,
								   peerId,
								   downloaded,
								   uploaded,
								   left,
								   port,
								   compact,
								   event);
	}

}
