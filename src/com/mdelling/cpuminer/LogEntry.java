package com.mdelling.cpuminer;

import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class LogEntry implements Parcelable {

	private Date timestamp;
	private int threads;
	private double hashrate;
	private long accepted;
	private long total;

	public LogEntry(int threads, double hashrate, long accepted, long total) {
		// Get the date
		this.timestamp = Calendar.getInstance().getTime();
		this.threads = threads;
		this.hashrate = hashrate;
		this.accepted = accepted;
		this.total = total;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(timestamp.getTime());
		out.writeInt(threads);
		out.writeDouble(hashrate);
		out.writeLong(accepted);
		out.writeLong(total);
	}

    public static final Parcelable.Creator<LogEntry> CREATOR
    		= new Parcelable.Creator<LogEntry>() {
    	@Override
		public LogEntry createFromParcel(Parcel in) {
    		return new LogEntry(in);
    	}

    	@Override
    	public LogEntry[] newArray(int size) {
    		return new LogEntry[size];
    	}
    };

    private LogEntry(Parcel in) {
    	timestamp = new Date(in.readLong());
    	threads = in.readInt();
    	hashrate = in.readDouble();
    	accepted = in.readLong();
    	total = in.readLong();
    }

    public Date getTimestamp() {
    	return timestamp;
    }

    public int getThreads() {
    	return threads;
    }

    public double getHashRate() {
    	return hashrate;
    }

    public long getBlocksAccepted() {
    	return accepted;
    }

    public long getBlocksTotal() {
    	return total;
    }
}
