package com.mojang.comm;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.net.ConnectionManager;
import com.mojang.minecraft.net.NetworkPlayer;
import com.mojang.minecraft.net.Packet;
import net.lax1dude.eaglercraft.internal.IWebSocketClient;
import net.lax1dude.eaglercraft.internal.IWebSocketFrame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class SocketConnection {
	public ByteBuffer readBuffer = ByteBuffer.allocate(1048576);
	public ByteBuffer writeBuffer = ByteBuffer.allocate(1048576);
	public ConnectionManager manager;
	public byte[] stringPacket = new byte[64];
	public IWebSocketClient webSocket;

	public SocketConnection(ConnectionManager var1) {
		this.manager = var1;
		this.readBuffer.clear();
		this.writeBuffer.clear();
	}

	public final void disconnect() {
		if (this.webSocket != null) {
			this.webSocket.close();
			this.webSocket = null;
		}
	}

	public final void processData() throws IOException {
		IWebSocketFrame packet = this.webSocket.getNextBinaryFrame();
		byte[] packetData = packet == null ? null : packet.getByteArray();

		if (packetData != null && packetData.length > 0) {
			readBuffer.put(packetData);
		}

		for (int var1 = 0; this.readBuffer.position() > 0 && var1++ != 100; this.readBuffer.compact()) {
			this.readBuffer.flip();
			byte var2 = this.readBuffer.get(0);
			Packet var3 = Packet.PACKETS[var2];
			if (var3 == null) {
				throw new IOException("Bad command: " + var2);
			}

			if (this.readBuffer.remaining() < var3.size + 1) {
				this.readBuffer.compact();
				return;
			}

			this.readBuffer.get();
			Object[] var11 = new Object[var3.fields.length];

			for (int var4 = 0; var4 < var11.length; ++var4) {
				Class var6 = var3.fields[var4];
				Object var10002;
				if (var6 == Long.TYPE) {
					var10002 = Long.valueOf(this.readBuffer.getLong());
				} else if (var6 == Integer.TYPE) {
					var10002 = Integer.valueOf(this.readBuffer.getInt());
				} else if (var6 == Short.TYPE) {
					var10002 = Short.valueOf(this.readBuffer.getShort());
				} else if (var6 == Byte.TYPE) {
					var10002 = Byte.valueOf(this.readBuffer.get());
				} else if (var6 == Double.TYPE) {
					var10002 = Double.valueOf(this.readBuffer.getDouble());
				} else if (var6 == Float.TYPE) {
					var10002 = Float.valueOf(this.readBuffer.getFloat());
				} else if (var6 == String.class) {
					this.readBuffer.get(this.stringPacket);
					var10002 = (new String(this.stringPacket, Charset.forName("UTF-8"))).trim();
				} else if (var6 == byte[].class) {
					byte[] var7 = new byte[1024];
					this.readBuffer.get(var7);
					var10002 = var7;
				} else {
					var10002 = null;
				}

				var11[var4] = var10002;
			}

			ConnectionManager var5 = this.manager;
			if (var3 == Packet.LOGIN) {
				var5.minecraft.beginLevelLoading("Connecting to the server..");
				System.out.println(var11[0]);
			} else if (var3 == Packet.LEVEL_INITIALIZE) {
				var5.minecraft.levelLoadUpdate("Loading level..");
				var5.minecraft.setLevel((Level) null);
				var5.levelBuffer = new ByteArrayOutputStream();
			} else if (var3 == Packet.LEVEL_DATA_CHUNK) {
				short var12 = ((Short) var11[0]).shortValue();
				byte[] var14 = (byte[]) ((byte[]) var11[1]);
				byte var18 = ((Byte) var11[2]).byteValue();
				var5.minecraft.setLoadingProgress(var18);
				var5.levelBuffer.write(var14, 0, var12);
			} else {
				short var19;
				short var23;
				if (var3 == Packet.LEVEL_FINALIZE) {
					try {
						var5.levelBuffer.close();
					} catch (IOException var10) {
						var10.printStackTrace();
					}

					byte[] var13 = LevelIO.loadBlocks(new ByteArrayInputStream(var5.levelBuffer.toByteArray()));
					var5.levelBuffer = null;
					short var16 = ((Short) var11[0]).shortValue();
					var19 = ((Short) var11[1]).shortValue();
					var23 = ((Short) var11[2]).shortValue();
					Level var25 = new Level();
					var25.setData(var16, var19, var23, var13);
					var5.minecraft.setLevel(var25);
				} else if (var3 == Packet.SET_TILE) {
					if (var5.minecraft.level != null) {
						var5.minecraft.level.setTile(((Short) var11[0]).shortValue(), ((Short) var11[1]).shortValue(), ((Short) var11[2]).shortValue(), ((Byte) var11[3]).byteValue());
					}
				} else {
					byte var8;
					byte var10001;
					byte var17;
					short var10003;
					short var10004;
					short var20;
					NetworkPlayer var22;
					if (var3 == Packet.PLAYER_JOIN) {
						var10001 = ((Byte) var11[0]).byteValue();
						String var30 = (String) var11[1];
						var10003 = ((Short) var11[2]).shortValue();
						var10004 = ((Short) var11[3]).shortValue();
						short var10005 = ((Short) var11[4]).shortValue();
						byte var10006 = ((Byte) var11[5]).byteValue();
						byte var9 = ((Byte) var11[6]).byteValue();
						var8 = var10006;
						short var26 = var10005;
						var23 = var10004;
						var20 = var10003;
						String var21 = var30;
						var17 = var10001;
						if (var17 >= 0) {
							var22 = new NetworkPlayer(var5.minecraft.level, var17, var21, (float) var20 / 32.0F, (float) var23 / 32.0F, (float) var26 / 32.0F, (float) (var8 * 360) / 256.0F, (float) (var9 * 360) / 256.0F);
							var5.players.put(Byte.valueOf(var17), var22);
							var5.minecraft.level.entities.add(var22);
						} else {
							var5.minecraft.player.moveTo((float) var20 / 32.0F, (float) var23 / 32.0F, (float) var26 / 32.0F, (float) (var8 * 360) / 256.0F, (float) (var9 * 360) / 256.0F);
						}
					} else if (var3 == Packet.PLAYER_MOVE) {
						var10001 = ((Byte) var11[0]).byteValue();
						short var32 = ((Short) var11[1]).shortValue();
						var10003 = ((Short) var11[2]).shortValue();
						var10004 = ((Short) var11[3]).shortValue();
						byte var31 = ((Byte) var11[4]).byteValue();
						var8 = ((Byte) var11[5]).byteValue();
						byte var27 = var31;
						var23 = var10004;
						var20 = var10003;
						var19 = var32;
						var17 = var10001;
						if (var17 >= 0) {
							NetworkPlayer var28 = (NetworkPlayer) var5.players.get(Byte.valueOf(var17));
							if (var28 != null) {
								var28.moveTo((float) var19 / 32.0F, (float) var20 / 32.0F, (float) var23 / 32.0F, (float) (var8 * 360) / 256.0F, (float) (var27 * 360) / 256.0F);
							}
						}
					} else if (var3 == Packet.PLAYER_DISCONNECT) {
						var17 = ((Byte) var11[0]).byteValue();
						if (var17 >= 0) {
							var22 = (NetworkPlayer) var5.players.remove(Byte.valueOf(var17));
							if (var22 != null) {
								var5.minecraft.level.entities.remove(var22);
							}
						}
					}
				}
			}
		}
	}

	public final void sendPacket(Packet var1, Object... var2) {
		this.writeBuffer.put(var1.id);

		for (int var3 = 0; var3 < var2.length; ++var3) {
			Class var10001 = var1.fields[var3];
			Object var6 = var2[var3];
			Class var5 = var10001;
			SocketConnection var4 = this;
			if (var5 == Long.TYPE) {
				this.writeBuffer.putLong(((Long) var6).longValue());
			} else if (var5 == Integer.TYPE) {
				this.writeBuffer.putInt(((Number) var6).intValue());
			} else if (var5 == Short.TYPE) {
				this.writeBuffer.putShort(((Number) var6).shortValue());
			} else if (var5 == Byte.TYPE) {
				this.writeBuffer.put(((Number) var6).byteValue());
			} else if (var5 == Double.TYPE) {
				this.writeBuffer.putDouble(((Double) var6).doubleValue());
			} else if (var5 == Float.TYPE) {
				this.writeBuffer.putFloat(((Float) var6).floatValue());
			} else {
				byte[] var7;
				if (var5 != String.class) {
					if (var5 == byte[].class) {
						var7 = (byte[]) ((byte[]) var6);
						if (var7.length < 1024) {
							var7 = Arrays.copyOf(var7, 1024);
						}
						this.writeBuffer.put(var7);
					}
				} else {
					var7 = ((String) var6).getBytes(Charset.forName("UTF-8"));
					Arrays.fill(this.stringPacket, (byte) 32);

					int var8;
					for (var8 = 0; var8 < 64 && var8 < var7.length; ++var8) {
						var4.stringPacket[var8] = var7[var8];
					}

					for (var8 = var7.length; var8 < 64; ++var8) {
						var4.stringPacket[var8] = 32;
					}

					var4.writeBuffer.put(var4.stringPacket);
				}
			}
		}
		flush();
	}

	public void flush() {
	    if (webSocket == null || !webSocket.isOpen()) {
	        return;
	    }
		int len = writeBuffer.position();
		if (len > 0) {
			writeBuffer.flip();
			byte[] data = new byte[len];
			writeBuffer.get(data);
			this.webSocket.send(data);
			writeBuffer.clear();
		}
	}
}
