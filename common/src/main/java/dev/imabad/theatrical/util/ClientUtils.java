package dev.imabad.theatrical.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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

        return new ArrayList<>(list);
    }

    public static BlockPos blockPos(Vec3 vec3){
        return new BlockPos((int) Math.round(vec3.x), (int) Math.floor(vec3.y), (int) Math.round(vec3.z));
    }

    public static BlockPos blockPosFloored(Vec3 vec3){
        return new BlockPos((int) Math.floor(vec3.x), (int) Math.floor(vec3.y), (int) Math.floor(vec3.z));
    }
}
