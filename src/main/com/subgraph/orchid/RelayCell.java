package com.subgraph.orchid;

import java.nio.ByteBuffer;



@SuppressWarnings("unused")
public interface RelayCell extends Cell {

	int LENGTH_OFFSET = 12;
	int RECOGNIZED_OFFSET = 4;
	int DIGEST_OFFSET = 8;
	int HEADER_SIZE = 14;

	int RELAY_BEGIN = 1;
	int RELAY_DATA = 2;
	int RELAY_END = 3;
	int RELAY_CONNECTED = 4;
	int RELAY_SENDME = 5;
	int RELAY_EXTEND = 6;
	int RELAY_EXTENDED = 7;
	int RELAY_TRUNCATE = 8;
	int RELAY_TRUNCATED = 9;
	int RELAY_DROP = 10;
	int RELAY_RESOLVE = 11;
	int RELAY_RESOLVED = 12;
	int RELAY_BEGIN_DIR = 13;
	int RELAY_EXTEND2 = 14;
	int RELAY_EXTENDED2 = 15;
	
    int RELAY_COMMAND_ESTABLISH_INTRO = 32;
    int RELAY_COMMAND_ESTABLISH_RENDEZVOUS = 33;
    int RELAY_COMMAND_INTRODUCE1 = 34;
    int RELAY_COMMAND_INTRODUCE2 = 35;
    int RELAY_COMMAND_RENDEZVOUS1 = 36;
    int RELAY_COMMAND_RENDEZVOUS2 = 37;
    int RELAY_COMMAND_INTRO_ESTABLISHED = 38;
    int RELAY_COMMAND_RENDEZVOUS_ESTABLISHED = 39;
    int RELAY_COMMAND_INTRODUCE_ACK = 40;

	int REASON_MISC = 1;
	int REASON_RESOLVEFAILED = 2;
	int REASON_CONNECTREFUSED = 3;
	int REASON_EXITPOLICY = 4;
	int REASON_DESTROY = 5;
	int REASON_DONE = 6;
	int REASON_TIMEOUT = 7;
	int REASON_NOROUTE = 8;
	int REASON_HIBERNATING = 9;
	int REASON_INTERNAL = 10;
	int REASON_RESOURCELIMIT = 11;
	int REASON_CONNRESET = 12;
	int REASON_TORPROTOCOL = 13;
	int REASON_NOTDIRECTORY = 14;

	int getStreamId();
	int getRelayCommand();
	/**
	 * Return the circuit node this cell was received from for outgoing cells or the destination circuit node
	 * for outgoing cells.
	 */
	CircuitNode getCircuitNode();
	ByteBuffer getPayloadBuffer();
	void setLength();
	void setDigest(byte[] digest);
}
