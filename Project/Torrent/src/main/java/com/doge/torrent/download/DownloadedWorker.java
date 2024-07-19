package com.doge.torrent.download;

import com.doge.torrent.connection.piece.PieceProgress;
import com.doge.torrent.download.files.TorrentSaver;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadedWorker implements Runnable {

	private final BlockingQueue<PieceProgress> downloadedQueue;
	private final Map<Integer, PieceProgress> piecesByIndex;
	private final AtomicBoolean hasFinished;
	private final TorrentSaver saver;
	private final byte[] buffer;
	private int downloaded = 0;
	private final int pieceCount;

	public DownloadedWorker(BlockingQueue<PieceProgress> downloadedQueue,
							Map<Integer, PieceProgress> piecesByIndex,
							AtomicBoolean hasFinished,
							TorrentSaver saver,
							Long length,
							int pieceCount) {
		this.downloadedQueue = downloadedQueue;
		this.piecesByIndex = piecesByIndex;
		this.hasFinished = hasFinished;
		this.saver = saver;
		this.buffer = new byte[length.intValue()];
		this.pieceCount = pieceCount;
	}

	public DownloadedWorker(BlockingQueue<PieceProgress> downloadedQueue,
							Map<Integer, PieceProgress> piecesByIndex,
							AtomicBoolean hasFinished,
							TorrentSaver saver,
							int length,
							int pieceCount) {
		this.downloadedQueue = downloadedQueue;
		this.piecesByIndex = piecesByIndex;
		this.hasFinished = hasFinished;
		this.saver = saver;
		this.buffer = new byte[length];
		this.pieceCount = pieceCount;
	}

	@Override
	public void run() {
		Long pieceLength = null;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				if (downloaded == pieceCount) {
					saver.save(buffer);
					hasFinished.set(true);
					break;
				}
				PieceProgress piece = downloadedQueue.take();
				if (pieceLength == null) {
					pieceLength = piece.pieceLength();
				}
				int destPos = (int) (piece.pieceIndex() * pieceLength);
				int length = getPieceLength(piece);
				System.arraycopy(piece.data(), 0, buffer, destPos, length);
				piecesByIndex.put(piece.pieceIndex(), piece);
				downloaded++;

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private int getPieceLength(PieceProgress piece) {
		return (int) Math.min(piece.pieceLength(),
				  buffer.length - (int) (piece.pieceIndex() * piece.pieceLength()));

	}
}
