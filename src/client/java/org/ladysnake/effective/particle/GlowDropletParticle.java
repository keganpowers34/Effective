package org.ladysnake.effective.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.ladysnake.effective.Effective;

public class GlowDropletParticle extends DropletParticle {
	public float redAndGreen = random.nextFloat() / 5f;
	public float blue = 1.0f;

	private GlowDropletParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
		super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
	}

	@Override
	public void tick() {
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;

		if (this.age++ >= this.maxAge) {
			this.markDead();
		}

		if (this.onGround || (this.age > 5 && this.world.getBlockState(BlockPos.ofFloored(this.x, this.y + this.velocityY, this.z)).getBlock() == Blocks.WATER)) {
			this.markDead();
		}

		if (this.world.getBlockState(BlockPos.ofFloored(this.x, this.y + this.velocityY, this.z)).getBlock() == Blocks.WATER && this.world.getBlockState(BlockPos.ofFloored(this.x, this.y, this.z)).isAir()) {
			for (int i = 0; i > -10; i--) {
				BlockPos pos = BlockPos.ofFloored(this.x, Math.round(this.y) + i, this.z);
				if (this.world.getBlockState(pos).getBlock() == Blocks.WATER && this.world.getBlockState(BlockPos.ofFloored(this.x, Math.round(this.y) + i, this.z)).getFluidState().isStill() && this.world.getBlockState(BlockPos.ofFloored(this.x, Math.round(this.y) + i + 1, this.z)).isAir()) {
					this.world.addParticle(Effective.GLOW_RIPPLE, this.x, Math.round(this.y) + i + 0.9f, this.z, 0, 0, 0);
					break;
				}
			}

			this.markDead();
		}

		this.velocityX *= 0.99f;
		this.velocityY -= 0.05f;
		this.velocityZ *= 0.99f;

		this.move(velocityX, velocityY, velocityZ);
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		Vec3d vec3d = camera.getPos();
		float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
		Quaternionf quaternion2;
		if (this.angle == 0.0F) {
			quaternion2 = camera.getRotation();
		} else {
			quaternion2 = new Quaternionf(camera.getRotation());
			float i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
			quaternion2.rotateZ(i);
		}

		Vector3f Vec3f = new Vector3f(-1.0F, -1.0F, 0.0F);
		Vec3f.rotate(quaternion2);
		Vector3f[] Vec3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
		float j = this.getSize(tickDelta);

		for (int k = 0; k < 4; ++k) {
			Vector3f Vec3f2 = Vec3fs[k];
			Vec3f2.rotate(quaternion2);
			Vec3f2.mul(j);
			Vec3f2.add(f, g, h);
		}

		float minU = this.getMinU();
		float maxU = this.getMaxU();
		float minV = this.getMinV();
		float maxV = this.getMaxV();

		int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		float redAndGreenRender = Math.min(1, redAndGreen + world.getLightLevel(LightType.BLOCK, BlockPos.ofFloored(x, y, z)) / 15f);

		vertexConsumer.vertex(Vec3fs[0].x(), Vec3fs[0].y(), Vec3fs[0].z()).texture(maxU, maxV).color(redAndGreenRender, redAndGreenRender, blue, alpha).light(l);
		vertexConsumer.vertex(Vec3fs[1].x(), Vec3fs[1].y(), Vec3fs[1].z()).texture(maxU, minV).color(redAndGreenRender, redAndGreenRender, blue, alpha).light(l);
		vertexConsumer.vertex(Vec3fs[2].x(), Vec3fs[2].y(), Vec3fs[2].z()).texture(minU, minV).color(redAndGreenRender, redAndGreenRender, blue, alpha).light(l);
		vertexConsumer.vertex(Vec3fs[3].x(), Vec3fs[3].y(), Vec3fs[3].z()).texture(minU, maxV).color(redAndGreenRender, redAndGreenRender, blue, alpha).light(l);
	}

	@Environment(EnvType.CLIENT)
	public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
		private final SpriteProvider spriteProvider;

		public DefaultFactory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
			return new GlowDropletParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
		}
	}
}
