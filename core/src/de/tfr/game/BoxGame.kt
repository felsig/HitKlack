package de.tfr.game

import de.tfr.game.model.GameField
import de.tfr.game.model.Orientation
import de.tfr.game.model.Ring
import de.tfr.game.model.Stone

/**
 * @author Tobse4Git@gmail.com
 */
class BoxGame(val field: GameField) : Controller.ControlListener {

    private var active: Stone
    private var time = 0f
    private var pause = false
    private var activeRing: Ring? = null

    init {
        active = Stone(field[field.size - 1][Orientation.Left])
    }

    override fun controlEvent(control: Controller.Control) {
        when (control) {
            mapOrientation(active.block.orientation) -> setStone()
            Controller.Control.Action -> setStone(active)
            Controller.Control.Esc -> reset()
            Controller.Control.Pause -> pause = !pause
        }
    }

    fun mapOrientation(orientation: Orientation): Controller.Control {
        when (orientation) {
            Orientation.Left -> return Controller.Control.Left
            Orientation.Right -> return Controller.Control.Right
            Orientation.Up -> return Controller.Control.Top
            Orientation.Down -> return Controller.Control.Bottom
        }
    }

    fun getStones() = listOf(active)

    fun update(deltaTime: Float) {
        time += deltaTime
        if (!pause && time >= 0.6f) {
            time = 0f
            move(active)
        }
    }

    private fun reset() {
        field.reset()
        activeRing = null
        respawnStone()
    }

    private fun respawnStone() {
        val field = field[field.size - 1][randomOrientation()]
        active = Stone(field)
    }

    private fun randomOrientation(): Orientation {
        if (activeRing != null) {
            val randomFreeSide = activeRing!!.randomFreeSide()
            if (randomFreeSide != null) {
                return randomFreeSide
            }
        }
        return Orientation.random()
    }

    private fun move(stone: Stone) {
        if (stone.isInLastRow()) {
            misstep()
        } else {
            val next = field[active.block.row - 1][active.block.orientation]
            active.block = next
        }
    }

    private fun Stone.isInLastRow() = this.block.row == 0
    private fun setStone() {
        if (active.block.isEmpty()) {
            setStone(active)
        }
    }

    private fun setStone(stone: Stone) {
        active.block.stone = active
        stone.freeze()
        if (activeRing != null) {
            if (activeRing!!.isFull()) {
                activeRing = null
            } else if (activeRing?.index != active.block.row) {
                misstep()
            }
        } else {
            activeRing = field[active.block.row]
        }
        respawnStone()
    }

    private fun misstep() {
        activeRing?.reset()
        active.block.reset()
        activeRing = null
        resetLastFullRing()
        respawnStone()
    }

    private fun firstFull(): Ring? {
        return field.find(Ring::isFull)
    }

    private fun resetLastFullRing() {
        firstFull()?.reset()
    }

}