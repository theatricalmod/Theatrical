package dev.imabad.theatrical.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientUtils {
    public static <E> List<E> optimize(List<E> list)
    {
        if (list.isEmpty())
        {
            return Collections.emptyList();
        }
        else if (list.size() == 1)
        {
            return Collections.singletonList(list.get(0));
        }

        return Arrays.asList(list.toArray((E[]) new Object[0]));
    }

    public static BlockPos fromVec(Vec3 vec3){
        return new BlockPos(Math.round(vec3.x), Math.floor(vec3.y), Math.round(vec3.z));
    }
}
