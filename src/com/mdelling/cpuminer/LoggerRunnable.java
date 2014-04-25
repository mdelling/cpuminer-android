package com.mdelling.cpuminer;


public class LoggerRunnable implements Runnable {

	private CPUMinerApplication application;

	public LoggerRunnable(CPUMinerApplication application) {
		this.application = application;
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Sleep for a bit
				Thread.sleep(60000);

				// Get the hash rate
				int threads = application.getThreads();
				double hashRate = 0;
				for (int i = 0; i < application.getThreads(); i++)
					hashRate += application.getHashRate(i) / 1000;

				// Get the block statistics
				long accepted = application.getAccepted();
				long total = accepted + application.getRejected();

				// Generate the LogEntry
				LogEntry entry = new LogEntry(threads, hashRate, accepted, total);
				application.updateApplication(entry);
			} catch (InterruptedException exp) {
				break;
			}
		}
	}
}
