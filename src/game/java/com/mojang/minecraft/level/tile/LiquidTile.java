package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.renderer.Tesselator;
import java.util.Random;

public class LiquidTile extends Tile {
	protected int liquidType;
	protected int calmTileId;
	protected int tileId;

	protected LiquidTile(int var1, int var2) {
		super(var1);
		this.liquidType = var2;
		this.tex = 14;
		if(var2 == 2) {
			this.tex = 30;
		}

		this.tileId = var1;
		this.calmTileId = var1 + 1;
		float var3 = 0.1F;
		this.setShape(0.0F, 0.0F - var3, 0.0F, 1.0F, 1.0F - var3, 1.0F);
		this.setTicking(true);
	}

	public final void onBlockAdded(Level var1, int var2, int var3, int var4) {
		var1.addToTickNextTick(var2, var3, var4, this.tileId);
	}

	public void tick(Level var1, int var2, int var3, int var4, Random var5) {
		boolean var7 = false;
		var4 = var4;
		var3 = var3;
		var2 = var2;
		var1 = var1;
		LiquidTile var8 = this;
		boolean var9 = false;

		boolean var6;
		do {
			--var3;
			if(var1.getTile(var2, var3, var4) != 0) {
				break;
			}

			var6 = var1.setTile(var2, var3, var4, var8.tileId);
			if(var6) {
				var9 = true;
			}
		} while(var6 && var8.liquidType != 2);

		++var3;
		if(var8.liquidType == 1 || !var9) {
			var9 |= var8.checkWater(var1, var2 - 1, var3, var4);
			var9 |= var8.checkWater(var1, var2 + 1, var3, var4);
			var9 |= var8.checkWater(var1, var2, var3, var4 - 1);
			var9 |= var8.checkWater(var1, var2, var3, var4 + 1);
		}

		if(!var9) {
			var1.setTileNoUpdate(var2, var3, var4, var8.calmTileId);
		} else {
			var1.addToTickNextTick(var2, var3, var4, var8.tileId);
		}

	}

	private boolean checkWater(Level var1, int var2, int var3, int var4) {
		int var5 = var1.getTile(var2, var3, var4);
		if(var5 == 0) {
			boolean var6 = var1.setTile(var2, var3, var4, this.tileId);
			if(var6) {
				var1.addToTickNextTick(var2, var3, var4, this.tileId);
			}
		}

		return false;
	}

	protected final boolean shouldRenderFace(Level var1, int var2, int var3, int var4, int var5, int var6) {
		if(var2 >= 0 && var3 >= 0 && var4 >= 0 && var2 < var1.width && var4 < var1.height) {
			if(var5 != 1 && this.liquidType == 1) {
				return false;
			} else {
				var5 = var1.getTile(var2, var3, var4);
				return var5 != this.tileId && var5 != this.calmTileId ? super.shouldRenderFace(var1, var2, var3, var4, -1, var6) : false;
			}
		} else {
			return false;
		}
	}

	public final void renderFace(Tesselator var1, int var2, int var3, int var4, int var5) {
		super.renderFace(var1, var2, var3, var4, var5);
		super.renderBackFace(var1, var2, var3, var4, var5);
	}

	public final boolean mayPick() {
		return false;
	}

	public final AABB getAABB(int var1, int var2, int var3) {
		return null;
	}

	public final boolean blocksLight() {
		return true;
	}

	public final boolean isSolid() {
		return false;
	}

	public final int getLiquidType() {
		return this.liquidType;
	}

	public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
		if(this.liquidType == 1 && (var5 == Tile.lava.id || var5 == Tile.calmLava.id)) {
			var1.setTileNoUpdate(var2, var3, var4, Tile.rock.id);
		}

		if(this.liquidType == 2 && (var5 == Tile.water.id || var5 == Tile.calmWater.id)) {
			var1.setTileNoUpdate(var2, var3, var4, Tile.rock.id);
		}

		var1.addToTickNextTick(var2, var3, var4, var5);
	}
}
