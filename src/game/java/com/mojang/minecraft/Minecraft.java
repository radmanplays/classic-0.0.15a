package com.mojang.minecraft;

import com.mojang.minecraft.character.Vec3;
import com.mojang.minecraft.character.Zombie;
import com.mojang.minecraft.gui.Font;
import com.mojang.minecraft.gui.PauseScreen;
import com.mojang.minecraft.gui.Screen;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.levelgen.LevelGen;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleEngine;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.MovementInputFromOptions;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.renderer.Chunk;
import com.mojang.minecraft.renderer.Frustum;
import com.mojang.minecraft.renderer.LevelRenderer;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;
import com.mojang.util.GLAllocation;
import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.EagUtils;
import com.mojang.minecraft.renderer.DirtyChunkSorter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import net.lax1dude.eaglercraft.internal.EnumPlatformType;
import net.lax1dude.eaglercraft.internal.buffer.FloatBuffer;
import net.lax1dude.eaglercraft.internal.buffer.IntBuffer;
import net.lax1dude.eaglercraft.internal.vfs2.VFile2;

public final class Minecraft implements Runnable {
	private boolean fullscreen = false;
	public int width;
	public int height;
	private FloatBuffer fogColor0 = GLAllocation.createFloatBuffer(4);
	private FloatBuffer fogColor1 = GLAllocation.createFloatBuffer(4);
	private Timer timer = new Timer(20.0F);
	private Level level;
	private LevelRenderer levelRenderer;
	private Player player;
	private int paintTexture = 1;
	private ParticleEngine particleEngine;
	public User user = null;
	private int yMouseAxis = 1;
	private Textures textures;
	public Font font;
	private int editMode = 0;
	private Screen screen = null;
	private LevelIO levelIo = new LevelIO(this);
	private LevelGen levelGen = new LevelGen(this);
	private int ticksRan = 0;
	public String loadMapUser = null;
	public int loadMapID = 0;
	private static final int[] creativeTiles = new int[]{Tile.rock.id, Tile.dirt.id, Tile.stoneBrick.id, Tile.wood.id, Tile.bush.id, Tile.log.id, Tile.leaf.id, Tile.sand.id, Tile.gravel.id};
	private float fogColorRed = 0.5F;
	private float fogColorGreen = 0.8F;
	private float fogColorBlue = 1.0F;
	private volatile boolean running = false;
	private String fpsString = "";
	private boolean mouseGrabbed = false;
	private int prevFrameTime = 0;
	private float renderDistance = 0.0F;
	private IntBuffer viewportBuffer = GLAllocation.createIntBuffer(16);
	private IntBuffer selectBuffer = GLAllocation.createIntBuffer(2000);
	private HitResult hitResult = null;
	private volatile int unusedInt1 = 0;
	private volatile int unusedInt2 = 0;
	private FloatBuffer lb = GLAllocation.createFloatBuffer(16);
	private String title = "";
	private String text = "";
	
	public Minecraft(int var2, int var3, boolean var4) {
		this.width = width;
		this.height = height;
		this.fullscreen = false;
		this.textures = new Textures();
	}
	
	public final void setScreen(Screen var1) {
		if(this.screen != null) {
			this.screen.closeScreen();
		}

		this.screen = var1;
		if(var1 != null) {
			int var2 = this.width * 240 / this.height;
			int var3 = this.height * 240 / this.height;
			var1.init(this, var2, var3);
		}

	}
	
	private static void checkGlError(String string) {
		int errorCode = GL11.glGetError();
		if(errorCode != 0) {
			String errorString = GLU.gluErrorString(errorCode);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + string);
			System.out.println(errorCode + ": " + errorString);
			throw new RuntimeException(errorCode + ": " + errorString);

		}

	}

	public final void destroy() {
		Minecraft var2 = this;
		try {
			LevelIO.save(var2.level, new VFile2("level.dat"));
		} catch (Exception var1) {
			var1.printStackTrace();
		}
		EagRuntime.destroy();
	}

	public final void run() {
		this.running = true;

		try {
			Minecraft var4 = this;
			this.fogColor0.put(new float[]{this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F});
			this.fogColor0.flip();
			this.fogColor1.put(new float[]{(float)14 / 255.0F, (float)11 / 255.0F, (float)10 / 255.0F, 1.0F});
			this.fogColor1.flip();
			if(this.fullscreen) {
				Display.toggleFullscreen();
				this.width = Display.getWidth();
				this.height = Display.getHeight();
			} else {
				this.width = Display.getWidth();
				this.height = Display.getHeight();
			}

			Display.setTitle("Minecraft 0.0.14a_08");

			Display.create();
			Keyboard.create();
			Mouse.create();

			checkGlError("Pre startup");
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glClearDepth(1.0D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			GL11.glCullFace(GL11.GL_BACK);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			checkGlError("Startup");
			this.font = new Font("/default.gif", this.textures);
			IntBuffer var8 = GLAllocation.createIntBuffer(256);
			var8.clear().limit(256);
			GL11.glViewport(0, 0, this.width, this.height);
			boolean var9 = false;

			try {
					Level var10 = null;
					var10 = var4.levelIo.load(new VFile2("level.dat"));
					var9 = var10 != null;
					if(!var9) {
						var10 = var4.levelIo.loadLegacy(new VFile2("level.dat"));
						var9 = var10 != null;
					}

					var4.setLevel(var10);
			} catch (Exception var20) {
				var20.printStackTrace();
				var9 = false;
			}

			if(!var9) {
				this.generateLevel(1);
			}

			this.levelRenderer = new LevelRenderer(this.textures);
			this.particleEngine = new ParticleEngine(this.level, this.textures);
			this.player = new Player(this.level, new MovementInputFromOptions());
			this.player.resetPos();
			if(this.level != null) {
				this.levelRenderer.setLevel(this.level);
			}

			checkGlError("Post startup");
		} catch (Exception var26) {
			var26.printStackTrace();
			System.out.println("Failed to start Minecraft");
			return;
		}

		long var1 = System.currentTimeMillis();
		int var3 = 0;

		try {
			while(this.running) {
					if(Display.isCloseRequested()) {
						this.running = false;
					}

					Timer var27 = this.timer;
					long var7 = System.nanoTime();
					long var29 = var7 - var27.lastTime;
					var27.lastTime = var7;
					if(var29 < 0L) {
						var29 = 0L;
					}

					if(var29 > 1000000000L) {
						var29 = 1000000000L;
					}

					var27.fps += (float)var29 * var27.timeScale * var27.ticksPerSecond / 1.0E9F;
					var27.ticks = (int)var27.fps;
					if(var27.ticks > 100) {
						var27.ticks = 100;
					}

					var27.fps -= (float)var27.ticks;
					var27.a = var27.fps;

					for(int var28 = 0; var28 < this.timer.ticks; ++var28) {
						++this.ticksRan;
						this.tick();
					}

					checkGlError("Pre render");
					this.render(this.timer.a);
					checkGlError("Post render");
					++var3;

					while(System.currentTimeMillis() >= var1 + 1000L) {
						this.fpsString = var3 + " fps, " + Chunk.updates + " chunk updates";
						Chunk.updates = 0;
						var1 += 1000L;
						var3 = 0;
					}
				}

			return;
		} catch (Exception var24) {
			var24.printStackTrace();
		} finally {
			this.destroy();
		}

	}
	
	public final void stop() {
		this.running = false;
	}

	public final void grabMouse() {
		if(!this.mouseGrabbed) {
			this.mouseGrabbed = true;
			Mouse.setGrabbed(true);
			this.setScreen((Screen)null);
		}
	}
	
	private void releaseMouse() {
		if(this.mouseGrabbed) {
			this.player.releaseAllKeys();
			this.mouseGrabbed = false;
			Mouse.setGrabbed(false);
			this.setScreen(new PauseScreen());
		}
	}
	
	private int saveCountdown = 600;

	private void levelSave() {
	    if (level == null) return;

	    saveCountdown--;
	    if (saveCountdown <= 0) {
	    	LevelIO.save(this.level, new VFile2("level.dat"));
	        saveCountdown = 600;
	    }
	}
	

	private void clickMouse() {
		if(this.hitResult != null) {
			Tile var1 = Tile.tiles[this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z)];
			if(this.editMode == 0) {
				boolean var7 = this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
				if(var1 != null && var7) {
					var1.destroy(this.level, this.hitResult.x, this.hitResult.y, this.hitResult.z, this.particleEngine);
				}

			} else {
				int var2 = this.hitResult.x;
				int var6 = this.hitResult.y;
				int var3 = this.hitResult.z;
				if(this.hitResult.f == 0) {
					--var6;
				}

				if(this.hitResult.f == 1) {
					++var6;
				}

				if(this.hitResult.f == 2) {
					--var3;
				}

				if(this.hitResult.f == 3) {
					++var3;
				}

				if(this.hitResult.f == 4) {
					--var2;
				}

				if(this.hitResult.f == 5) {
					++var2;
				}

				Tile var4 = Tile.tiles[this.level.getTile(var2, var6, var3)];
				if(var4 == null || var4 == Tile.water || var4 == Tile.calmWater || var4 == Tile.lava || var4 == Tile.calmLava) {
					AABB var8 = Tile.tiles[this.paintTexture].getAABB(var2, var6, var3);
					if(var8 == null || (this.player.bb.intersects(var8) ? false : this.level.isFree(var8))) {
						this.level.setTile(var2, var6, var3, this.paintTexture);
						Tile.tiles[this.paintTexture].onBlockAdded(this.level, var2, var6, var3);
					}
				}

			}
		}
	}
	
	private void tick() {
		int var2;
		LevelRenderer var6;
		if(this.screen != null) {
			this.prevFrameTime = this.ticksRan + 10000;
		} else {
			while(true) {
				int var1;
				if(Mouse.isMouseGrabbed() || Mouse.isActuallyGrabbed()) {
					this.mouseGrabbed = true;
				}
				while(Mouse.next()) {
					var1 = Mouse.getEventDWheel();
					int var3;
					Minecraft var5;
					if(var1 != 0) {
						var2 = var1;
						var5 = this;
						if(var1 > 0) {
							var2 = 1;
						}

						if(var2 < 0) {
							var2 = -1;
						}

						var3 = 0;

						for(int var4 = 0; var4 < creativeTiles.length; ++var4) {
							if(creativeTiles[var4] == var5.paintTexture) {
								var3 = var4;
							}
						}

						for(var3 += var2; var3 < 0; var3 += creativeTiles.length) {
						}

						while(var3 >= creativeTiles.length) {
							var3 -= creativeTiles.length;
						}

						var5.paintTexture = creativeTiles[var3];
					}

					if(!this.mouseGrabbed && Mouse.getEventButtonState()) {
						this.grabMouse();
					} else {
						if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
							this.clickMouse();
							this.prevFrameTime = this.ticksRan;
						}

						if(Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
							this.editMode = (this.editMode + 1) % 2;
						}

						if(Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
							var5 = this;
							if(this.hitResult != null) {
								var2 = this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z);
								if(var2 == Tile.grass.id) {
									var2 = Tile.dirt.id;
								}

								for(var3 = 0; var3 < creativeTiles.length; ++var3) {
									if(var2 == creativeTiles[var3]) {
										var5.paintTexture = creativeTiles[var3];
									}
								}
							}
						}
					}
				}

				while(Keyboard.next()) {
					this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
					if(Keyboard.getEventKeyState()) {
						if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
							this.releaseMouse();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_R) {
							this.player.resetPos();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
							this.level.setSpawnPos((int)this.player.x, (int)this.player.y, (int)this.player.z, this.player.yRot);
							this.player.resetPos();
						}

						for(var1 = 0; var1 < 9; ++var1) {
							if(Keyboard.getEventKey() == var1 + 2) {
								this.paintTexture = creativeTiles[var1];
							}
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_Y) {
							this.yMouseAxis = -this.yMouseAxis;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_G && this.level.entities.size() < 256) {
							this.level.entities.add(new Zombie(this.level, this.player.x, this.player.y, this.player.z));
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_F) {
							var6 = this.levelRenderer;
							var6.drawDistance = (var6.drawDistance + 1) % 4;
						}
					}
				}

				if(Mouse.isButtonDown(0) && (float)(this.ticksRan - this.prevFrameTime) >= this.timer.ticksPerSecond / 4.0F && this.mouseGrabbed) {
					this.clickMouse();
					this.prevFrameTime = this.ticksRan;
				}
				break;
			}
		}

		if(this.screen != null) {
			this.screen.updateEvents();
			if(this.screen != null) {
				this.screen.tick();
			}
		}

		var6 = this.levelRenderer;
		++var6.cloudTickCounter;
		this.level.tick();
		ParticleEngine var7 = this.particleEngine;

		for(var2 = 0; var2 < var7.particles.size(); ++var2) {
			Particle var8 = (Particle)var7.particles.get(var2);
			var8.tick();
			if(var8.removed) {
				var7.particles.remove(var2--);
			}
		}

		this.player.tick();
		levelSave();
	}

	private void orientCamera(float var1) {
		GL11.glTranslatef(0.0F, 0.0F, -0.3F);
		GL11.glRotatef(this.player.xRot - this.player.xRotI * (1.0F - var1), 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(this.player.yRot - this.player.yRotI * (1.0F - var1), 0.0F, 1.0F, 0.0F);
		float var2 = this.player.xo + (this.player.x - this.player.xo) * var1;
		float var3 = this.player.yo + (this.player.y - this.player.yo) * var1;
		float var4 = this.player.zo + (this.player.z - this.player.zo) * var1;
		GL11.glTranslatef(-var2, -var3, -var4);
	}

	private void render(float var1) {
		if(!Display.isActive()) {
			this.releaseMouse();
		}
		if (Display.wasResized()) {
			this.width = Display.getWidth();
			this.height = Display.getHeight();
			
			if(this.screen != null) {
				Screen sc = this.screen;
				this.setScreen((Screen)null);
				this.setScreen(sc);
			}
		}
		GL11.glViewport(0, 0, this.width, this.height);
		int var2;
		int var3;
		int var4;
		int var5;
		if(this.mouseGrabbed) {
			var2 = 0;
			var3 = 0;
			var2 = Mouse.getDX();
			var3 = Mouse.getDY();

			this.player.turn((float)var2, (float)(var3 * this.yMouseAxis));
		}

		GL11.glViewport(0, 0, this.width, this.height);
		checkGlError("Set viewport");
		float pitch = this.player.xRot;
		float yaw = this.player.yRot;

		double px = this.player.x;
		double py = this.player.y;
		double pz = this.player.z;

		Vec3 cameraPos = new Vec3((float)px, (float)py, (float)pz);

		float cosYaw = (float)Math.cos(-Math.toRadians(yaw) - Math.PI);
		float sinYaw = (float)Math.sin(-Math.toRadians(yaw) - Math.PI);
		float cosPitch = (float)Math.cos(-Math.toRadians(pitch));
		float sinPitch = (float)Math.sin(-Math.toRadians(pitch));

		float dirX = sinYaw * cosPitch;
		float dirY = sinPitch;
		float dirZ = cosYaw * cosPitch;
		float reachDistance = 3.0F;
		if (pitch > 60.0F) {
		    reachDistance += 1.0F;
		}
		if (pitch >= 55.0F && pitch <= 60.0F) {
		    reachDistance += 2.0F;
		}
		Vec3 reachVec = new Vec3(
		    cameraPos.x + dirX * reachDistance,
		    cameraPos.y + dirY * reachDistance,
		    cameraPos.z + dirZ * reachDistance
		);

		this.hitResult = this.level.clip(cameraPos, reachVec);
		checkGlError("Picked");
		this.fogColorRed = 0.92F;
		this.fogColorGreen = 0.98F;
		this.fogColorBlue = 1.0F;
		GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		this.renderDistance = (float)(1024 >> (this.levelRenderer.drawDistance << 1));
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, this.renderDistance);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
	    if (!Display.isActive() || !Mouse.isMouseGrabbed() || !Mouse.isActuallyGrabbed()) {
	        if (System.currentTimeMillis() - prevFrameTime > 250L) {
	            if (this.screen == null) {
	            	releaseMouse();
	            }
	        }
	    }
	    
		this.orientCamera(var1);
		checkGlError("Set up camera");
		GL11.glEnable(GL11.GL_CULL_FACE);
		Frustum var23 = Frustum.getFrustum();
		Frustum var24 = var23;
		LevelRenderer var18 = this.levelRenderer;

		for(var5 = 0; var5 < var18.sortedChunks.length; ++var5) {
			var18.sortedChunks[var5].isInFrustum(var24);
		}

		Player var19 = this.player;
		var18 = this.levelRenderer;
		List<Chunk> var28 = new ArrayList<>(var18.dirtyChunks);
		var28.sort(new DirtyChunkSorter(var19));
		var28.addAll(var18.dirtyChunks);
		int var25 = 4;
		Iterator var29 = var28.iterator();

		while(var29.hasNext()) {
			Chunk var30 = (Chunk)var29.next();
			var30.rebuild();
			var18.dirtyChunks.remove(var30);
			--var25;
			if(var25 == 0) {
				break;
			}
		}


		checkGlError("Update chunks");
		boolean var21 = this.level.isSolid(this.player.x, this.player.y, this.player.z, 0.1F);
		this.setupFog(0);
		GL11.glEnable(GL11.GL_FOG);
		this.levelRenderer.render(this.player, 0);
		if(var21) {
			var4 = (int)this.player.x;
			var5 = (int)this.player.y;
			var25 = (int)this.player.z;

			for(var2 = var4 - 1; var2 <= var4 + 1; ++var2) {
				for(int var7 = var5 - 1; var7 <= var5 + 1; ++var7) {
					for(int var8 = var25 - 1; var8 <= var25 + 1; ++var8) {
						this.levelRenderer.render(var2, var7, var8);
					}
				}
			}
		}

		checkGlError("Rendered level");
		this.levelRenderer.renderEntities(var23, var1);
		checkGlError("Rendered entities");
		this.particleEngine.render(this.player, var1);
		checkGlError("Rendered particles");
		var18 = this.levelRenderer;
		var18.renderSurroundingGround();
		GL11.glDisable(GL11.GL_LIGHTING);
		this.setupFog(-1);
		this.levelRenderer.renderClouds(var1);
		this.setupFog(1);
		GL11.glEnable(GL11.GL_LIGHTING);
		if(this.hitResult != null) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			this.levelRenderer.renderHit(this.player, this.hitResult, this.editMode, this.paintTexture);
			LevelRenderer.renderHitOutline(this.hitResult, this.editMode);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		this.setupFog(0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		var18 = this.levelRenderer;
		GL11.glCallList(var18.surroundLists + 1);
		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glColorMask(false, false, false, false);
//		this.levelRenderer.render(this.player, 1);
//		GL11.glColorMask(true, true, true, true);
		this.levelRenderer.render(this.player, 1);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		if(this.hitResult != null) {
			GL11.glDepthFunc(GL11.GL_LESS);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
//			this.levelRenderer.renderHit(this.player, this.hitResult, this.editMode, this.paintTexture);
			LevelRenderer.renderHitOutline(this.hitResult, this.editMode);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		}
		var4 = this.width * 240 / this.height;
		var5 = this.height * 240 / this.height;
		var25 = Mouse.getX() * var4 / this.width;
		int var7 = var5 - Mouse.getY() * var5 / this.height - 1;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)var4, (double)var5, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		checkGlError("GUI: Init");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)(var4 - 16), 16.0F, -50.0F);
		Tesselator var32 = Tesselator.instance;
		GL11.glScalef(16.0F, 16.0F, 16.0F);
		GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
		GL11.glScalef(-1.0F, -1.0F, -1.0F);
		var3 = this.textures.loadTexture("/terrain.png", 9728);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, var3);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		var32.begin();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Tile.tiles[this.paintTexture].render(var32, this.level, 0, -2, 0, 0);
		var32.end();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
		checkGlError("GUI: Draw selected");
		this.font.drawShadow("0.0.14a_08", 2, 2, 16777215);
		this.font.drawShadow(this.fpsString, 2, 12, 16777215);
		checkGlError("GUI: Draw text");
		var4 /= 2;
		var3 = var5 / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		var32.begin();
		var32.vertex((float)(var4 + 1), (float)(var3 - 4), 0.0F);
		var32.vertex((float)var4, (float)(var3 - 4), 0.0F);
		var32.vertex((float)var4, (float)(var3 + 5), 0.0F);
		var32.vertex((float)(var4 + 1), (float)(var3 + 5), 0.0F);
		var32.vertex((float)(var4 + 5), (float)var3, 0.0F);
		var32.vertex((float)(var4 - 4), (float)var3, 0.0F);
		var32.vertex((float)(var4 - 4), (float)(var3 + 1), 0.0F);
		var32.vertex((float)(var4 + 5), (float)(var3 + 1), 0.0F);
		var32.end();
		checkGlError("GUI: Draw crosshair");
		if(this.screen != null) {
			this.screen.render(var25, var7);
		}

		checkGlError("Rendered gui");
		Display.update();
	}
	
	private void setupFog(int var1) {
		if(var1 == -1) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, this.renderDistance);
			GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
//			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
		} else {
			Tile var2 = Tile.tiles[this.level.getTile((int)this.player.x, (int)(this.player.y + 0.12F), (int)this.player.z)];
			if(var2 != null && var2.getLiquidType() == 1) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
				GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(0.02F, 0.02F, 0.2F, 1.0F));
//				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(0.3F, 0.3F, 0.7F, 1.0F));
			} else if(var2 != null && var2.getLiquidType() == 2) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
				GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(0.6F, 0.1F, 0.0F, 1.0F));
//				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(0.4F, 0.3F, 0.3F, 1.0F));
			} else if(var1 == 0) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
				GL11.glFogf(GL11.GL_FOG_START, 0.0F);
				GL11.glFogf(GL11.GL_FOG_END, this.renderDistance);
				GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
//				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
			} else if(var1 == 1) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.01F);
				GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor1);
				float var3 = 0.6F;
//				GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(var3, var3, var3, 1.0F));
			}

//			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
//			GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	private FloatBuffer getBuffer(float a, float b, float c, float d) {
		this.lb.clear();
		this.lb.put(a).put(b).put(c).put(d);
		this.lb.flip();
		return this.lb;
	}

	public final void beginLevelLoading(String var1) {
		this.title = var1;
	    if (this.height == 0) {
	        return;
	    }
		int var3 = this.width * 240 / this.height;
		int var2 = this.height * 240 / this.height;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)var3, (double)var2, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
	}

	public final void levelLoadUpdate(String var1) {
		this.text = var1;
		this.setLoadingProgress(-1);
	}

	public final void setLoadingProgress(int var1) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    if (this.height == 0) {
	        return;
	    }
		int var2 = this.width * 240 / this.height;
		int var3 = this.height * 240 / this.height;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		Tesselator var4 = Tesselator.instance;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		int var5 = this.textures.loadTexture("/dirt.png", 9728);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, var5);
		float var8 = 32.0F;
		var4.begin();
		var4.color(4210752);
		var4.vertexUV(0.0F, (float)var3, 0.0F, 0.0F, (float)var3 / var8);
		var4.vertexUV((float)var2, (float)var3, 0.0F, (float)var2 / var8, (float)var3 / var8);
		var4.vertexUV((float)var2, 0.0F, 0.0F, (float)var2 / var8, 0.0F);
		var4.vertexUV(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		var4.end();
		if(var1 >= 0) {
			var5 = var2 / 2 - 50;
			int var6 = var3 / 2 + 16;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			var4.begin();
			var4.color(8421504);
			var4.vertex((float)var5, (float)var6, 0.0F);
			var4.vertex((float)var5, (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + 100), (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + 100), (float)var6, 0.0F);
			var4.color(8454016);
			var4.vertex((float)var5, (float)var6, 0.0F);
			var4.vertex((float)var5, (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + var1), (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + var1), (float)var6, 0.0F);
			var4.end();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		this.font.drawShadow(this.title, (var2 - this.font.width(this.title)) / 2, var3 / 2 - 4 - 16, 16777215);
		this.font.drawShadow(this.text, (var2 - this.font.width(this.text)) / 2, var3 / 2 - 4 + 8, 16777215);
		Display.update();
	}

	public final void generateLevel(int var1) {
		String var2 = this.user != null ? this.user.name : "anonymous";
		this.setLevel(this.levelGen.generateLevel(var2, 128 << var1, 128 << var1, 64));
	}

	private void setLevel(Level var1) {
		this.level = var1;
		if(this.levelRenderer != null) {
			this.levelRenderer.setLevel(var1);
		}

		if(this.particleEngine != null) {
			ParticleEngine var2 = this.particleEngine;
			var2.particles.clear();
		}

		if(this.player != null) {
			this.player.setLevel(var1);
			this.player.resetPos();
		}

		System.gc();
	}
}
