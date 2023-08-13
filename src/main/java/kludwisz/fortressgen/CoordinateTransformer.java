package kludwisz.fortressgen;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import com.seedfinding.mccore.util.pos.BPos;

public class CoordinateTransformer 
{
	public static BlockDirection direction = null;
	public static BlockBox boundingBox = null;
	
	public static final void setParams(BlockDirection _direction, BlockBox _boundingBox) {
		direction = _direction;
		boundingBox = _boundingBox;
	}
	
	public static final BPos getWorldPos(int relativeX, int relativeY, int relativeZ) {
		return new BPos(getWorldX(relativeX, relativeZ), getWorldY(relativeY), getWorldZ(relativeX, relativeZ));
	}
	
	public static final int getWorldX(int relativeX, int relativeZ) {
        if (direction == null) {
            return relativeX;
        } else {
            switch(direction) {
                case NORTH:
                case SOUTH:
                    return boundingBox.minX + relativeX;
                case WEST:
                    return boundingBox.maxX - relativeZ;
                case EAST:
                    return boundingBox.minX + relativeZ;
                default:
                    return relativeX;
            }
        }
    }

	public static final int getWorldY(int relativeY) {
        return direction == null ? relativeY : relativeY + boundingBox.minY;
    }

	public static final int getWorldZ(int relativeX, int relativeZ) {
        if (direction == null) {
            return relativeZ;
        } else {
            switch(direction) {
                case NORTH:
                    return boundingBox.maxZ - relativeZ;
                case SOUTH:
                    return boundingBox.minZ + relativeZ;
                case WEST:
                case EAST:
                    return boundingBox.minZ + relativeX;
                default:
                    return relativeZ;
            }
        }
    }
}

