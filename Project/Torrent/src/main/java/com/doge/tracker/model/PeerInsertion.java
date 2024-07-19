package com.doge.tracker.model;

import com.doge.torrent.announce.model.Peer;
import com.doge.tracker.cleanup.Inserted;

import java.time.LocalDateTime;

public class  PeerInsertion implements Inserted {

	private Peer peer;

	private LocalDateTime insertionTime;

	public PeerInsertion(Peer peer, LocalDateTime insertionTime) {
		this.peer = peer;
		this.insertionTime = insertionTime;
	}

	@Override public LocalDateTime getInsertionTime() {
		return insertionTime;
	}

	public Peer getPeer() {
		return peer;
	}

	public void setInsertionTime(LocalDateTime insertionTime) {
		this.insertionTime = insertionTime;
	}

	@Override public String toString() {
		return "PeerInsertion{" +
			   "peer=" + peer +
			   ", insertionTime=" + insertionTime +
			   '}';
	}
}
