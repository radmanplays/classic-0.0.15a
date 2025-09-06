package com.mojang.minecraft;

public final class Timer {
	float ticksPerSecond;
	long lastTime;
	public int ticks;
	public float a;
	public float timeScale = 1.0F;
	public float fps = 0.0F;

	public Timer(float var1) {
		this.ticksPerSecond = var1;
		this.lastTime = System.nanoTime();
	}
}
