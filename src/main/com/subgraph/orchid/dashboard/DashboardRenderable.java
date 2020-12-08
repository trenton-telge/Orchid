package com.subgraph.orchid.dashboard;

import java.io.IOException;
import java.io.PrintWriter;

public interface DashboardRenderable {
	
	int DASHBOARD_CONNECTIONS           = 1;
	int DASHBOARD_CONNECTIONS_VERBOSE   = 1 << 1;
	int DASHBOARD_PREDICTED_PORTS       = 1 << 2;
	int DASHBOARD_CIRCUITS              = 1 << 3;
	int DASHBOARD_STREAMS               = 1 << 4;
	
	void dashboardRender(DashboardRenderer renderer, PrintWriter writer, int flags) throws IOException;
}
