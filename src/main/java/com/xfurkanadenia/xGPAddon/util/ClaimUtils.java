package com.xfurkanadenia.xGPAddon.util;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.World;

public class ClaimUtils {

    /**
     * Belirtilen alan içinde radius mesafesindeki claim'leri bulur
     * Gerçek Euclidean mesafe hesaplama kullanır
     */
    public static Claim getClaimNearby(Location lesser, Location greater, double radius) {
        
        if (lesser == null || greater == null || lesser.getWorld() == null) {
            return null;
        }

        World world = lesser.getWorld();

        
        if (GriefPrevention.instance == null || GriefPrevention.instance.dataStore == null) {
            return null;
        }

        int newMinX = Math.min(lesser.getBlockX(), greater.getBlockX());
        int newMinZ = Math.min(lesser.getBlockZ(), greater.getBlockZ());
        int newMaxX = Math.max(lesser.getBlockX(), greater.getBlockX());
        int newMaxZ = Math.max(lesser.getBlockZ(), greater.getBlockZ());

        try {
            for (Claim existingClaim : GriefPrevention.instance.dataStore.getClaims()) {
                
                if (existingClaim == null ||
                        existingClaim.getLesserBoundaryCorner() == null ||
                        existingClaim.getGreaterBoundaryCorner() == null ||
                        existingClaim.getLesserBoundaryCorner().getWorld() == null) {
                    continue;
                }

                if (!existingClaim.getLesserBoundaryCorner().getWorld().equals(world)) continue;

                int existingMinX = existingClaim.getLesserBoundaryCorner().getBlockX();
                int existingMinZ = existingClaim.getLesserBoundaryCorner().getBlockZ();
                int existingMaxX = existingClaim.getGreaterBoundaryCorner().getBlockX();
                int existingMaxZ = existingClaim.getGreaterBoundaryCorner().getBlockZ();

                
                double distance = getDistanceBetweenRectangles(
                        newMinX, newMinZ, newMaxX, newMaxZ,
                        existingMinX, existingMinZ, existingMaxX, existingMaxZ
                );

                if (distance <= radius) {
                    return existingClaim;
                }
            }
        } catch (Exception e) {
            
            
            return null;
        }

        return null;
    }

    /**
     * İki dikdörtgen arasındaki en kısa Euclidean mesafeyi hesaplar
     */
    private static double getDistanceBetweenRectangles(
            int rect1MinX, int rect1MinZ, int rect1MaxX, int rect1MaxZ,
            int rect2MinX, int rect2MinZ, int rect2MaxX, int rect2MaxZ) {

        
        int deltaX = Math.max(0, Math.max(rect1MinX - rect2MaxX, rect2MinX - rect1MaxX));

        
        int deltaZ = Math.max(0, Math.max(rect1MinZ - rect2MaxZ, rect2MinZ - rect1MaxZ));

        
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    /**
     * Alternatif: Manhattan mesafesi kullanarak (daha hızlı hesaplama)
     */
    public static Claim getClaimNearbyManhattan(Location lesser, Location greater, double radius) {
        World world = lesser.getWorld();

        int newMinX = Math.min(lesser.getBlockX(), greater.getBlockX());
        int newMinZ = Math.min(lesser.getBlockZ(), greater.getBlockZ());
        int newMaxX = Math.max(lesser.getBlockX(), greater.getBlockX());
        int newMaxZ = Math.max(lesser.getBlockZ(), greater.getBlockZ());

        for (Claim existingClaim : GriefPrevention.instance.dataStore.getClaims()) {
            if (!existingClaim.getLesserBoundaryCorner().getWorld().equals(world)) continue;

            int existingMinX = existingClaim.getLesserBoundaryCorner().getBlockX();
            int existingMinZ = existingClaim.getLesserBoundaryCorner().getBlockZ();
            int existingMaxX = existingClaim.getGreaterBoundaryCorner().getBlockX();
            int existingMaxZ = existingClaim.getGreaterBoundaryCorner().getBlockZ();

            
            int distanceX = Math.max(0, Math.max(existingMinX - newMaxX, newMinX - existingMaxX));
            int distanceZ = Math.max(0, Math.max(existingMinZ - newMaxZ, newMinZ - existingMaxZ));
            double manhattanDistance = distanceX + distanceZ;

            if (manhattanDistance <= radius) {
                return existingClaim;
            }
        }

        return null;
    }

    /**
     * Chebyshev mesafesi kullanarak (kare şeklinde radius)
     */
    public static Claim getClaimNearbyChebyshev(Location lesser, Location greater, double radius) {
        World world = lesser.getWorld();

        int newMinX = Math.min(lesser.getBlockX(), greater.getBlockX());
        int newMinZ = Math.min(lesser.getBlockZ(), greater.getBlockZ());
        int newMaxX = Math.max(lesser.getBlockX(), greater.getBlockX());
        int newMaxZ = Math.max(lesser.getBlockZ(), greater.getBlockZ());

        for (Claim existingClaim : GriefPrevention.instance.dataStore.getClaims()) {
            if (!existingClaim.getLesserBoundaryCorner().getWorld().equals(world)) continue;

            int existingMinX = existingClaim.getLesserBoundaryCorner().getBlockX();
            int existingMinZ = existingClaim.getLesserBoundaryCorner().getBlockZ();
            int existingMaxX = existingClaim.getGreaterBoundaryCorner().getBlockX();
            int existingMaxZ = existingClaim.getGreaterBoundaryCorner().getBlockZ();

            
            int distanceX = Math.max(0, Math.max(existingMinX - newMaxX, newMinX - existingMaxX));
            int distanceZ = Math.max(0, Math.max(existingMinZ - newMaxZ, newMinZ - existingMaxZ));
            double chebyshevDistance = Math.max(distanceX, distanceZ);

            if (chebyshevDistance <= radius) {
                return existingClaim;
            }
        }

        return null;
    }
}