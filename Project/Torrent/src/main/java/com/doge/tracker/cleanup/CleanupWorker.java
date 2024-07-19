package com.doge.tracker.cleanup;

import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CleanupWorker<T extends Inserted> implements Runnable {
	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(CleanupWorker.class);

	private static final int MILLIS_TO_SECONDS = 1000;

	private final Long deletionInterval;

	private final Map<?, List<T>> insertedMap;

	public CleanupWorker(Long deletionInterval, Map<?, List<T>> insertedMap) {
		this.deletionInterval = deletionInterval;
		this.insertedMap = insertedMap;
	}

	@Override public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			cleanup();
			try {
				Thread.sleep(deletionInterval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	private void cleanup() {
		insertedMap.values().forEach(this::cleanList);
	}

	private void cleanList(List<T> list) {
		list.removeIf(this::shouldDelete);
	}

	private boolean shouldDelete(T inserted) {
		boolean shouldDelete = inserted.getInsertionTime()
				.isBefore(LocalDateTime.now()
				  	.minusSeconds(deletionInterval / MILLIS_TO_SECONDS)
			    );
		if (shouldDelete) {
			LOGGER.debug("Deleted " + inserted);
		}
		return shouldDelete;
	}
}
