package extracells.part.gas

import java.util

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.storage.data.IAEFluidStack
import extracells.integration.Integration
import extracells.part.fluid.PartFluidExport
import extracells.util.GasUtil
import mekanism.api.gas.IGasHandler
import net.minecraftforge.fluids.{Fluid, FluidStack}
import net.minecraftforge.fml.common.Optional

class PartGasExport extends PartFluidExport {

  private val isMekanismEnabled = Integration.Mods.MEKANISMGAS.isEnabled


  override def doWork(rate: Int, tickSinceLastCall: Int): Boolean = {
    if (isMekanismEnabled)
      work(rate, tickSinceLastCall)
    else
      false
  }


  @Optional.Method(modid = "MekanismAPI|gas")
  protected def work(rate: Int, ticksSinceLastCall: Int): Boolean = {
    val facingTank: IGasHandler = getFacingGasTank
    if (facingTank == null || !isActive) return false
    val filter = new util.ArrayList[Fluid]
    filter.add(this.filterFluids(4))

    if (this.filterSize >= 1) {
      {
        var i: Byte = 1
        while (i < 9) {
          {
            if (i != 4) {
              filter.add(this.filterFluids(i))
            }
          }
          i = (i + 2).toByte
        }
      }
    }

    if (this.filterSize >= 2) {
      {
        var i: Byte = 0
        while (i < 9) {
          {
            if (i != 4) {
              filter.add(this.filterFluids(i))
            }
          }
          i = (i + 2).toByte
        }
      }
    }
    import scala.collection.JavaConversions._
    for (fluid <- filter) {
      if (fluid != null) {
        val stack: IAEFluidStack = extractGas(AEApi.instance.storage.createFluidStack(new FluidStack(fluid, rate * ticksSinceLastCall)), Actionable.SIMULATE)

        if (stack != null) {
          val gasStack = GasUtil.getGasStack(stack.getFluidStack)
          if (gasStack != null && facingTank.canReceiveGas(getFacing.getOpposite, gasStack.getGas)) {
            val filled: Int = facingTank.receiveGas(getFacing.getOpposite, gasStack, true)
            if (filled > 0) {
              extractGas(AEApi.instance.storage.createFluidStack(new FluidStack(fluid, filled)), Actionable.MODULATE)
              return true
            }
          }
        }
      }
    }
    return false
  }


}
