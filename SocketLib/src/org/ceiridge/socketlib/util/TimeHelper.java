package org.ceiridge.socketlib.util;

public class TimeHelper {
	/**
	 * Last time in milliseconds since this Timer instance was reset
	 */
	private long lastTime;

	/**
	 * Calls the reset method so the lastTime isn't null or 0.
	 */
	public TimeHelper() {
		reset();
	}

	/**
	 * Converts the current java.lang.System#nanoTime to milliseconds and returns it.
	 * 
	 * @return Current system time in milliseconds.
	 */
	public long getCurrentTime() {
		return System.nanoTime() / 1000000;
	}

	/**
	 * @return Last time in milliseconds since this Timer instance was reset
	 */
	public long getLastTime() {
		return lastTime;
	}

	/**
	 * Subtracts the lastTime from the current system time in milliseconds.
	 * 
	 * @return the difference between lastTime and the current system time in milliseconds.
	 */
	public long getDifference() {
		return getCurrentTime() - lastTime;
	}

	/**
	 * Resets the lastTime.
	 */
	public void reset() {
		lastTime = getCurrentTime();
	}

	/**
	 * Determine if this Timer instance has reached a difference in milliseconds.
	 * 
	 * @param milliseconds the amount of milliseconds that the Timer instance has to have reached
	 * @return if the Timer instance has reached milliseconds
	 */
	public boolean hasReached(long milliseconds) {
		return getDifference() >= milliseconds;
	}
}
