package com.doge.torrent.connection;

import com.doge.torrent.announce.model.Peer;
import com.doge.torrent.connection.message.Message;
import com.doge.torrent.connection.piece.PieceProgress;
import com.doge.torrent.files.model.TorrentPiece;

public interface ClientConnector {

	void connect(Peer peer);

	void disconnect();

	PieceProgress downloadPiece(TorrentPiece piece);

	void sendMessage(Message message);

	Message readMessage();

	boolean isDisconnected();
}
