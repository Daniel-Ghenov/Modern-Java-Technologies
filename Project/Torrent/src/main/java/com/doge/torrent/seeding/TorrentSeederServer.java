package com.doge.torrent.seeding;

import com.doge.torrent.logging.Logger;
import com.doge.torrent.logging.TorrentLoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TorrentSeederServer {

	private static final Logger LOGGER = TorrentLoggerFactory.getLogger(TorrentSeederServer.class);
	private final TorrentSeeder seeder;
	private final String peerId;
	private final int port;
	private boolean running = true;

	public TorrentSeederServer(TorrentSeeder seeder, String peerId, int port) {
		this.seeder = seeder;
		this.peerId = peerId;
		this.port = port;
	}

	public void start() {
		Executor executor = Executors.newVirtualThreadPerTaskExecutor();

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			LOGGER.info("Seeder server started on port " + port);
			Socket socket;
			while (running) {
				socket = serverSocket.accept();
				LOGGER.info("New connection from " + socket.getInetAddress());
				executor.execute(new SeederWorker(peerId, socket, seeder));
			}
		} catch (IOException e) {
			LOGGER.error("Error while starting seeder server", e);
		}
	}

	public void stop() {
		running = false;
	}
}
