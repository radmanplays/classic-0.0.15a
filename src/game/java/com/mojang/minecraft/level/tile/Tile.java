package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleEngine;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.renderer.Tesselator;
import java.util.Random;

public class Tile {
	public static final Tile[] tiles = new Tile[256];
	public static final boolean[] shouldTick = new boolean[256];
	public static final Tile rock = new Tile(1, 1);
	public static final Tile grass = new GrassTile(2);
	public static final Tile dirt = new DirtTile(3, 2);
	public static final Tile stoneBrick = new Tile(4, 16);
	public static final Tile wood = new Tile(5, 4);
	public static final Tile bush = new Bush(6);
	public static final Tile unbreakable = new Tile(7, 17);
	public static final Tile water = new LiquidTile(8, 1);
	public static final Tile calmWater = new CalmLiquidTile(9, 1);
	public static final Tile lava = new LiquidTile(10, 2);
	public static final Tile calmLava = new CalmLiquidTile(11, 2);
	public static final Tile sand = new FallingTile(12, 18);
	public static final Tile gravel = new FallingTile(13, 19);
	public static final Tile oreGold = new Tile(14, 32);
	public static final Tile oreIron = new Tile(15, 33);
	public static final Tile oreCoal = new Tile(16, 34);
	public static final Tile log = new LogTile(17);
	public static final Tile leaf = new LeafTile(18, 22);
	public int tex;
	public final int id;
	private float xx0;
	private float yy0;
	private float zz0;
	private float xx1;
	private float yy1;
	private float zz1;

	protected Tile(int var1) {
		new Random();
		tiles[var1] = this;
		this.id = var1;
		this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	protected final void setTicking(boolean var1) {
		shouldTick[this.id] = var1;
	}

	protected final void setShape(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.xx0 = 0.0F;
		this.yy0 = var2;
		this.zz0 = 0.0F;
		this.xx1 = 1.0F;
		this.yy1 = var5;
		this.zz1 = 1.0F;
	}

	protected Tile(int var1, int var2) {
		this(var1);
		this.tex = var2;
	}

	public boolean render(Tesselator var1, Level var2, int var3, int var4, int var5, int var6) {
		boolean var7 = false;
		float var8 = 0.8F;
		float var9 = 0.6F;
		float var10;
		if(this.shouldRenderFace(var2, var4, var5 - 1, var6, var3, 0)) {
			var10 = var2.getBrightness(var4, var5 - 1, var6);
			var1.color(var10 * 1.0F, var10 * 1.0F, var10 * 1.0F);
			this.renderFace(var1, var4, var5, var6, 0);
			var7 = true;
		}

		if(this.shouldRenderFace(var2, var4, var5 + 1, var6, var3, 1)) {
			var10 = var2.getBrightness(var4, var5 + 1, var6);
			var1.color(var10 * 1.0F, var10 * 1.0F, var10 * 1.0F);
			this.renderFace(var1, var4, var5, var6, 1);
			var7 = true;
		}

		if(this.shouldRenderFace(var2, var4, var5, var6 - 1, var3, 2)) {
			var10 = var2.getBrightness(var4, var5, var6 - 1);
			var1.color(var8 * var10, var8 * var10, var8 * var10);
			this.renderFace(var1, var4, var5, var6, 2);
			var7 = true;
		}

		if(this.shouldRenderFace(var2, var4, var5, var6 + 1, var3, 3)) {
			var10 = var2.getBrightness(var4, var5, var6 + 1);
			var1.color(var8 * var10, var8 * var10, var8 * var10);
			this.renderFace(var1, var4, var5, var6, 3);
			var7 = true;
		}

		if(this.shouldRenderFace(var2, var4 - 1, var5, var6, var3, 4)) {
			var10 = var2.getBrightness(var4 - 1, var5, var6);
			var1.color(var9 * var10, var9 * var10, var9 * var10);
			this.renderFace(var1, var4, var5, var6, 4);
			var7 = true;
		}

		if(this.shouldRenderFace(var2, var4 + 1, var5, var6, var3, 5)) {
			var10 = var2.getBrightness(var4 + 1, var5, var6);
			var1.color(var9 * var10, var9 * var10, var9 * var10);
			this.renderFace(var1, var4, var5, var6, 5);
			var7 = true;
		}

		return var7;
	}

	public static boolean cullFace(Level var0, int var1, int var2, int var3, int var4) {
		if(var4 == 0) {
			--var2;
		}

		if(var4 == 1) {
			++var2;
		}

		if(var4 == 2) {
			--var3;
		}

		if(var4 == 3) {
			++var3;
		}

		if(var4 == 4) {
			--var1;
		}

		if(var4 == 5) {
			++var1;
		}

		return !var0.isSolidTile(var1, var2, var3);
	}

	protected boolean shouldRenderFace(Level var1, int var2, int var3, int var4, int var5, int var6) {
		return var5 == 1 ? false : !var1.isSolidTile(var2, var3, var4);
	}

	protected int getTexture(int var1) {
		return this.tex;
	}

	public void renderFace(Tesselator var1, int var2, int var3, int var4, int var5) {
		int var6 = this.getTexture(var5);
		int var7 = var6 % 16 << 4;
		var6 = var6 / 16 << 4;
		float var8 = (float)var7 / 256.0F;
		float var17 = ((float)var7 + 15.99F) / 256.0F;
		float var9 = (float)var6 / 256.0F;
		float var16 = ((float)var6 + 15.99F) / 256.0F;
		float var10 = (float)var2 + this.xx0;
		float var14 = (float)var2 + this.xx1;
		float var11 = (float)var3 + this.yy0;
		float var15 = (float)var3 + this.yy1;
		float var12 = (float)var4 + this.zz0;
		float var13 = (float)var4 + this.zz1;
		if(var5 == 0) {
			var1.vertexUV(var10, var11, var13, var8, var16);
			var1.vertexUV(var10, var11, var12, var8, var9);
			var1.vertexUV(var14, var11, var12, var17, var9);
			var1.vertexUV(var14, var11, var13, var17, var16);
		} else if(var5 == 1) {
			var1.vertexUV(var14, var15, var13, var17, var16);
			var1.vertexUV(var14, var15, var12, var17, var9);
			var1.vertexUV(var10, var15, var12, var8, var9);
			var1.vertexUV(var10, var15, var13, var8, var16);
		} else if(var5 == 2) {
			var1.vertexUV(var10, var15, var12, var17, var9);
			var1.vertexUV(var14, var15, var12, var8, var9);
			var1.vertexUV(var14, var11, var12, var8, var16);
			var1.vertexUV(var10, var11, var12, var17, var16);
		} else if(var5 == 3) {
			var1.vertexUV(var10, var15, var13, var8, var9);
			var1.vertexUV(var10, var11, var13, var8, var16);
			var1.vertexUV(var14, var11, var13, var17, var16);
			var1.vertexUV(var14, var15, var13, var17, var9);
		} else if(var5 == 4) {
			var1.vertexUV(var10, var15, var13, var17, var9);
			var1.vertexUV(var10, var15, var12, var8, var9);
			var1.vertexUV(var10, var11, var12, var8, var16);
			var1.vertexUV(var10, var11, var13, var17, var16);
		} else if(var5 == 5) {
			var1.vertexUV(var14, var11, var13, var8, var16);
			var1.vertexUV(var14, var11, var12, var17, var16);
			var1.vertexUV(var14, var15, var12, var17, var9);
			var1.vertexUV(var14, var15, var13, var8, var9);
		}
	}

	public final void renderBackFace(Tesselator var1, int var2, int var3, int var4, int var5) {
		int var6 = this.getTexture(var5);
		float var7 = (float)(var6 % 16) / 16.0F;
		float var8 = var7 + 0.999F / 16.0F;
		float var16 = (float)(var6 / 16) / 16.0F;
		float var9 = var16 + 0.999F / 16.0F;
		float var10 = (float)var2 + this.xx0;
		float var14 = (float)var2 + this.xx1;
		float var11 = (float)var3 + this.yy0;
		float var15 = (float)var3 + this.yy1;
		float var12 = (float)var4 + this.zz0;
		float var13 = (float)var4 + this.zz1;
		if(var5 == 0) {
			var1.vertexUV(var14, var11, var13, var8, var9);
			var1.vertexUV(var14, var11, var12, var8, var16);
			var1.vertexUV(var10, var11, var12, var7, var16);
			var1.vertexUV(var10, var11, var13, var7, var9);
		}

		if(var5 == 1) {
			var1.vertexUV(var10, var15, var13, var7, var9);
			var1.vertexUV(var10, var15, var12, var7, var16);
			var1.vertexUV(var14, var15, var12, var8, var16);
			var1.vertexUV(var14, var15, var13, var8, var9);
		}

		if(var5 == 2) {
			var1.vertexUV(var10, var11, var12, var8, var9);
			var1.vertexUV(var14, var11, var12, var7, var9);
			var1.vertexUV(var14, var15, var12, var7, var16);
			var1.vertexUV(var10, var15, var12, var8, var16);
		}

		if(var5 == 3) {
			var1.vertexUV(var14, var15, var13, var8, var16);
			var1.vertexUV(var14, var11, var13, var8, var9);
			var1.vertexUV(var10, var11, var13, var7, var9);
			var1.vertexUV(var10, var15, var13, var7, var16);
		}

		if(var5 == 4) {
			var1.vertexUV(var10, var11, var13, var8, var9);
			var1.vertexUV(var10, var11, var12, var7, var9);
			var1.vertexUV(var10, var15, var12, var7, var16);
			var1.vertexUV(var10, var15, var13, var8, var16);
		}

		if(var5 == 5) {
			var1.vertexUV(var14, var15, var13, var7, var16);
			var1.vertexUV(var14, var15, var12, var8, var16);
			var1.vertexUV(var14, var11, var12, var8, var9);
			var1.vertexUV(var14, var11, var13, var7, var9);
		}

	}

	public static void renderFaceNoTexture(Player var0, Tesselator var1, int var2, int var3, int var4, int var5) {
		float var6 = (float)var2;
		float var7 = (float)var2 + 1.0F;
		float var8 = (float)var3;
		float var9 = (float)var3 + 1.0F;
		float var10 = (float)var4;
		float var11 = (float)var4 + 1.0F;
		if(var5 == 0 && (float)var3 > var0.y) {
			var1.vertex(var6, var8, var11);
			var1.vertex(var6, var8, var10);
			var1.vertex(var7, var8, var10);
			var1.vertex(var7, var8, var11);
		}

		if(var5 == 1 && (float)var3 < var0.y) {
			var1.vertex(var7, var9, var11);
			var1.vertex(var7, var9, var10);
			var1.vertex(var6, var9, var10);
			var1.vertex(var6, var9, var11);
		}

		if(var5 == 2 && (float)var4 > var0.z) {
			var1.vertex(var6, var9, var10);
			var1.vertex(var7, var9, var10);
			var1.vertex(var7, var8, var10);
			var1.vertex(var6, var8, var10);
		}

		if(var5 == 3 && (float)var4 < var0.z) {
			var1.vertex(var6, var9, var11);
			var1.vertex(var6, var8, var11);
			var1.vertex(var7, var8, var11);
			var1.vertex(var7, var9, var11);
		}

		if(var5 == 4 && (float)var2 > var0.x) {
			var1.vertex(var6, var9, var11);
			var1.vertex(var6, var9, var10);
			var1.vertex(var6, var8, var10);
			var1.vertex(var6, var8, var11);
		}

		if(var5 == 5 && (float)var2 < var0.x) {
			var1.vertex(var7, var8, var11);
			var1.vertex(var7, var8, var10);
			var1.vertex(var7, var9, var10);
			var1.vertex(var7, var9, var11);
		}

	}

	public static AABB getTileAABB(int var0, int var1, int var2) {
		return new AABB((float)var0, (float)var1, (float)var2, (float)(var0 + 1), (float)(var1 + 1), (float)(var2 + 1));
	}

	public AABB getAABB(int var1, int var2, int var3) {
		return new AABB((float)var1, (float)var2, (float)var3, (float)(var1 + 1), (float)(var2 + 1), (float)(var3 + 1));
	}

	public boolean blocksLight() {
		return true;
	}

	public boolean isSolid() {
		return true;
	}

	public boolean mayPick() {
		return true;
	}

	public void tick(Level var1, int var2, int var3, int var4, Random var5) {
	}

	public final void destroy(Level var1, int var2, int var3, int var4, ParticleEngine var5) {
		for(int var6 = 0; var6 < 4; ++var6) {
			for(int var7 = 0; var7 < 4; ++var7) {
				for(int var8 = 0; var8 < 4; ++var8) {
					float var9 = (float)var2 + ((float)var6 + 0.5F) / (float)4;
					float var10 = (float)var3 + ((float)var7 + 0.5F) / (float)4;
					float var11 = (float)var4 + ((float)var8 + 0.5F) / (float)4;
					Particle var12 = new Particle(var1, var9, var10, var11, var9 - (float)var2 - 0.5F, var10 - (float)var3 - 0.5F, var11 - (float)var4 - 0.5F, this.tex);
					var5.particles.add(var12);
				}
			}
		}

	}

	public int getLiquidType() {
		return 0;
	}

	public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
	}

	public void onBlockAdded(Level var1, int var2, int var3, int var4) {
	}
}
