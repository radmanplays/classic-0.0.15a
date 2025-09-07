package com.mojang.minecraft.net;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.character.ZombieModel;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.renderer.Textures;
import org.lwjgl.opengl.GL11;

public class NetworkPlayer extends Entity {
	public static final long serialVersionUID = 77479605454997290L;
	private static ZombieModel playerModel = new ZombieModel();
	private int ticks = 0;

	public NetworkPlayer(Level var1, int var2, String var3, float var4, float var5, float var6, float var7, float var8) {
		super(var1);
		this.setPos(var4, var5, var6);
		this.xRot = var8;
		this.yRot = var7;
		this.heightOffset = 1.62F;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		++this.ticks;
	}

	public void render(Textures var1, float var2) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, var1.loadTexture("/char.png", GL11.GL_NEAREST));
		GL11.glPushMatrix();
		float var5 = ((float)this.ticks + var2) / 2.0F;
		float var3 = this.getBrightness();
		GL11.glColor3f(var3, var3, var3);
		var3 = 0.058333334F;
		float var4 = (float)(-Math.abs(Math.sin((double)var5 * 0.6662D)) * 5.0D - 23.0D);
		GL11.glTranslatef(this.xo + (this.x - this.xo) * var2, this.yo + (this.y - this.yo) * var2 - this.heightOffset, this.zo + (this.z - this.zo) * var2);
		GL11.glScalef(1.0F, -1.0F, 1.0F);
		GL11.glScalef(var3, var3, var3);
		GL11.glTranslatef(0.0F, var4, 0.0F);
		GL11.glRotatef(-this.yRot, 0.0F, 1.0F, 0.0F);
		playerModel.render(var5, this.xRot);
		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
