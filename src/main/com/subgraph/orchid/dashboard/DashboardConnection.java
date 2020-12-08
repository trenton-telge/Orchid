package com.subgraph.orchid.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardConnection implements Runnable {
	
	private final static int REFRESH_INTERVAL = 1000;

	private final Dashboard dashboard;
	private final Socket socket;
	private final ScheduledExecutorService refreshExecutor;
	
	public DashboardConnection(Dashboard dashboard, Socket socket) {
		this.dashboard = dashboard;
		this.socket = socket;
		this.refreshExecutor = new ScheduledThreadPoolExecutor(1);
	}

	public void run() {
		ScheduledFuture<?> handle = null;
		try {
			final PrintWriter writer = new PrintWriter(socket.getOutputStream());
			handle = refreshExecutor.scheduleAtFixedRate(createRefreshRunnable(writer), 0, REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
			runInputLoop(socket.getInputStream());
		} catch (IOException e) {
			closeQuietly(socket);
		} finally {
			if(handle != null) {
				handle.cancel(true);
			}
			refreshExecutor.shutdown();
		}
	}

	private void closeQuietly(Socket s) {
		try {
			s.close();
		} catch (IOException ignored) { }
	}

	private void runInputLoop(InputStream input) throws IOException {
		int c;
		
		while((c = input.read()) != -1) {
			switch(c) {
			case 'c':
				toggleFlagWithVerbose();
				break;
			case 'p':
				toggleFlag();
				break;
			default:
				break;
			}
		}
	}

	// Rotate between 3 states
	//    0 (no flags),
	//    basicFlag,
	//    basicFlag|verboseFlag
	private void toggleFlagWithVerbose() {
		if(dashboard.isEnabled(DashboardRenderable.DASHBOARD_CONNECTIONS_VERBOSE)) {
			dashboard.disableFlag(DashboardRenderable.DASHBOARD_CONNECTIONS | DashboardRenderable.DASHBOARD_CONNECTIONS_VERBOSE);
		} else if(dashboard.isEnabled(DashboardRenderable.DASHBOARD_CONNECTIONS)) {
			dashboard.enableFlag(DashboardRenderable.DASHBOARD_CONNECTIONS_VERBOSE);
		} else {
			dashboard.enableFlag(DashboardRenderable.DASHBOARD_CONNECTIONS);
		}
	}
	
	private void toggleFlag() {
		if(dashboard.isEnabled(DashboardRenderable.DASHBOARD_PREDICTED_PORTS)) {
			dashboard.disableFlag(DashboardRenderable.DASHBOARD_PREDICTED_PORTS);
		} else {
			dashboard.enableFlag(DashboardRenderable.DASHBOARD_PREDICTED_PORTS);
		}
	}

	private void hideCursor(Writer writer) throws IOException {
		emitCSI(writer);
		writer.write("?25l");
	}

	private void emitCSI(Writer writer) throws IOException {
		writer.append((char) 0x1B);
		writer.append('[');
	}
	
	private void clear(PrintWriter writer) throws IOException {
		emitCSI(writer);
		writer.write("2J");
	}
	
	private void moveTo(PrintWriter writer) throws IOException {
		emitCSI(writer);
		writer.printf("%d;%dH", 1, 1);
	}
	
	private void refresh(PrintWriter writer) {
		try {
			if(socket.isClosed()) {
				return;
			}
			hideCursor(writer);
			clear(writer);
			moveTo(writer);
			dashboard.renderAll(writer);
			writer.flush();
		} catch(IOException e) {
			closeQuietly(socket);
		}
	}

	private Runnable createRefreshRunnable(final PrintWriter writer) {
		return () -> refresh(writer);
	}
}
