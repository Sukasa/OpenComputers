package li.cil.oc.client.renderer

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.client.Textures
import li.cil.oc.common
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.ExtendedAABB._
import li.cil.oc.util.ExtendedBlock._
import li.cil.oc.util.ExtendedWorld._
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

import scala.util.Random

object HighlightRenderer {
  private val random = new Random()

  lazy val tablet = api.Items.get(Constants.ItemName.Tablet)

  @SubscribeEvent
  def onDrawBlockHighlight(e: DrawBlockHighlightEvent): Unit = if (e.target != null && e.target.getBlockPos != null) {
    val hitInfo = e.target
    val world = e.player.getEntityWorld
    val blockPos = BlockPosition(hitInfo.getBlockPos, world)
    if (hitInfo.typeOfHit == MovingObjectType.BLOCK && api.Items.get(e.currentItem) == tablet) {
      val isAir = world.isAirBlock(blockPos)
      if (!isAir) {
        val block = world.getBlock(blockPos)
        block.setBlockBoundsBasedOnState(blockPos)
        val bounds = block.getSelectedBoundingBox(world, hitInfo.getBlockPos).offset(-blockPos.x, -blockPos.y, -blockPos.z)
        val sideHit = hitInfo.sideHit
        val playerPos = new Vec3(
          e.player.prevPosX + (e.player.posX - e.player.prevPosX) * e.partialTicks,
          e.player.prevPosY + (e.player.posY - e.player.prevPosY) * e.partialTicks,
          e.player.prevPosZ + (e.player.posZ - e.player.prevPosZ) * e.partialTicks)
        val renderPos = blockPos.offset(-playerPos.xCoord, -playerPos.yCoord, -playerPos.zCoord)

        RenderState.pushMatrix()
        RenderState.pushAttrib()
        RenderState.makeItBlend()
        Textures.bind(Textures.Model.HologramEffect)

        RenderState.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
        RenderState.color(0.0F, 1.0F, 0.0F, 0.4F)

        GL11.glTranslated(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GL11.glScaled(1.002, 1.002, 1.002)

        if (Settings.get.hologramFlickerFrequency > 0 && random.nextDouble() < Settings.get.hologramFlickerFrequency) {
          val (sx, sy, sz) = (1 - math.abs(sideHit.getFrontOffsetX), 1 - math.abs(sideHit.getFrontOffsetY), 1 - math.abs(sideHit.getFrontOffsetZ))
          GL11.glScaled(1 + random.nextGaussian() * 0.01, 1 + random.nextGaussian() * 0.001, 1 + random.nextGaussian() * 0.01)
          GL11.glTranslated(random.nextGaussian() * 0.01 * sx, random.nextGaussian() * 0.01 * sy, random.nextGaussian() * 0.01 * sz)
        }

        val t = Tessellator.getInstance()
        val r = t.getWorldRenderer
        r.begin(7, DefaultVertexFormats.POSITION_TEX)
        sideHit match {
          case EnumFacing.UP =>
            r.pos(bounds.maxX, bounds.maxY + 0.002, bounds.maxZ).tex(bounds.maxZ * 16, bounds.maxX * 16).endVertex()
            r.pos(bounds.maxX, bounds.maxY + 0.002, bounds.minZ).tex(bounds.minZ * 16, bounds.maxX * 16).endVertex()
            r.pos(bounds.minX, bounds.maxY + 0.002, bounds.minZ).tex(bounds.minZ * 16, bounds.minX * 16).endVertex()
            r.pos(bounds.minX, bounds.maxY + 0.002, bounds.maxZ).tex(bounds.maxZ * 16, bounds.minX * 16).endVertex()
          case EnumFacing.DOWN =>
            r.pos(bounds.maxX, bounds.minY - 0.002, bounds.minZ).tex(bounds.minZ * 16, bounds.maxX * 16).endVertex()
            r.pos(bounds.maxX, bounds.minY - 0.002, bounds.maxZ).tex(bounds.maxZ * 16, bounds.maxX * 16).endVertex()
            r.pos(bounds.minX, bounds.minY - 0.002, bounds.maxZ).tex(bounds.maxZ * 16, bounds.minX * 16).endVertex()
            r.pos(bounds.minX, bounds.minY - 0.002, bounds.minZ).tex(bounds.minZ * 16, bounds.minX * 16).endVertex()
          case EnumFacing.EAST =>
            r.pos(bounds.maxX + 0.002, bounds.maxY, bounds.minZ).tex(bounds.minZ * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.maxX + 0.002, bounds.maxY, bounds.maxZ).tex(bounds.maxZ * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.maxX + 0.002, bounds.minY, bounds.maxZ).tex(bounds.maxZ * 16, bounds.minY * 16).endVertex()
            r.pos(bounds.maxX + 0.002, bounds.minY, bounds.minZ).tex(bounds.minZ * 16, bounds.minY * 16).endVertex()
          case EnumFacing.WEST =>
            r.pos(bounds.minX - 0.002, bounds.maxY, bounds.maxZ).tex(bounds.maxZ * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.minX - 0.002, bounds.maxY, bounds.minZ).tex(bounds.minZ * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.minX - 0.002, bounds.minY, bounds.minZ).tex(bounds.minZ * 16, bounds.minY * 16).endVertex()
            r.pos(bounds.minX - 0.002, bounds.minY, bounds.maxZ).tex(bounds.maxZ * 16, bounds.minY * 16).endVertex()
          case EnumFacing.SOUTH =>
            r.pos(bounds.maxX, bounds.maxY, bounds.maxZ + 0.002).tex(bounds.maxX * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.minX, bounds.maxY, bounds.maxZ + 0.002).tex(bounds.minX * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.minX, bounds.minY, bounds.maxZ + 0.002).tex(bounds.minX * 16, bounds.minY * 16).endVertex()
            r.pos(bounds.maxX, bounds.minY, bounds.maxZ + 0.002).tex(bounds.maxX * 16, bounds.minY * 16).endVertex()
          case _ =>
            r.pos(bounds.minX, bounds.maxY, bounds.minZ - 0.002).tex(bounds.minX * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.maxX, bounds.maxY, bounds.minZ - 0.002).tex(bounds.maxX * 16, bounds.maxY * 16).endVertex()
            r.pos(bounds.maxX, bounds.minY, bounds.minZ - 0.002).tex(bounds.maxX * 16, bounds.minY * 16).endVertex()
            r.pos(bounds.minX, bounds.minY, bounds.minZ - 0.002).tex(bounds.minX * 16, bounds.minY * 16).endVertex()
        }
        t.draw()

        RenderState.popAttrib()
        RenderState.popMatrix()
      }
    }

    if (hitInfo.typeOfHit == MovingObjectType.BLOCK) e.player.getEntityWorld.getTileEntity(hitInfo.getBlockPos) match {
      case print: common.tileentity.Print =>
        val pos = new Vec3(
          e.player.prevPosX + (e.player.posX - e.player.prevPosX) * e.partialTicks,
          e.player.prevPosY + (e.player.posY - e.player.prevPosY) * e.partialTicks,
          e.player.prevPosZ + (e.player.posZ - e.player.prevPosZ) * e.partialTicks)
        val expansion = 0.002f

        // See RenderGlobal.drawSelectionBox.
        GL11.glEnable(GL11.GL_BLEND)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        GL11.glColor4f(0, 0, 0, 0.4f)
        GL11.glLineWidth(2)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDepthMask(false)

        for (shape <- if (print.state) print.data.stateOn else print.data.stateOff) {
          val bounds = shape.bounds.rotateTowards(print.facing)
          RenderGlobal.func_181563_a(bounds.expand(expansion, expansion, expansion)
            .offset(blockPos.x, blockPos.y, blockPos.z)
            .offset(-pos.xCoord, -pos.yCoord, -pos.zCoord), 0xFF, 0xFF, 0xFF, 0xFF)
        }

        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)

        e.setCanceled(true)
      case _ =>
    }
  }
}
