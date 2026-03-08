package com.enargulus.movingblinds.listeners;

import com.enargulus.movingblinds.MovingBlinds;
import org.bukkit.event.block.Action;
import org.bukkit.event.Listener;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerListener implements Listener {

    private final MovingBlinds plugin;

    public PlayerListener(MovingBlinds plugin) {
        this.plugin = plugin;
    }

    private static boolean IsBanner(Block block) {
        switch (block.getType()) {
            case RED_WALL_BANNER:
            case BLUE_WALL_BANNER:
            case GREEN_WALL_BANNER:
            case YELLOW_WALL_BANNER:
            case BLACK_WALL_BANNER:
            case WHITE_WALL_BANNER:
            case ORANGE_WALL_BANNER:
            case PURPLE_WALL_BANNER:
            case CYAN_WALL_BANNER:
                return true;
            default:
                return false;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Handle player join event
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return; // ignore off-hand

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || PlayerListener.IsBanner(clickedBlock) == false)
            return;

        ArrayList<Block> adjacentBanners = new ArrayList<Block>();
        adjacentBanners.add(clickedBlock);

        for (int dir = 0; dir < 4; dir++) {
            BlockFace face = null;
            switch (dir) {
                case 1:
                    face = BlockFace.SOUTH;
                    break;
                case 2:
                    face = BlockFace.EAST;
                    break;
                case 3:
                    face = BlockFace.WEST;
                    break;
                default:
                    face = BlockFace.NORTH;
                    break;
            }

            int limit = 100; // Limit to prevent infinite loops
            Block currentBlock = clickedBlock;
            while (limit-- > 0) {
                Block newBlock = currentBlock.getRelative(face);

                if (!PlayerListener.IsBanner(newBlock) || adjacentBanners.contains(newBlock)) {
                    break;
                }

                BlockData newBlockData = newBlock.getBlockData();
                BlockData currentBlockData = currentBlock.getBlockData();
                if (newBlockData instanceof org.bukkit.block.data.Directional newDirectionalData
                        && currentBlockData instanceof org.bukkit.block.data.Directional currentDirectionalData) {
                    if (newDirectionalData.getFacing() != currentDirectionalData.getFacing())
                        break; // Stop if the facing direction is different
                } else
                    break; // Stop if block data is not directional

                currentBlock = newBlock;
                adjacentBanners.add(currentBlock);
            }
        }

        // determine if we're moving down or up
        boolean extending = clickedBlock.getRelative(BlockFace.DOWN).getType() != clickedBlock.getType()
                && clickedBlock.getRelative(BlockFace.UP).getType() != clickedBlock.getType();

        plugin.getLogger().info(
                "Found " + adjacentBanners.size() + " adjacent banners to " + (extending ? "extend" : "retract") + ".");

        // Move banners

        for (Block banner : adjacentBanners) { // one per column
            BlockData bannerData = banner.getBlockData().clone();

            ArrayList<Block> columnBlocks = new ArrayList<Block>();
            for (int dir = 0; dir < (extending == false ? 2 : 1); dir++) {
                final BlockFace face = dir == 0 ? BlockFace.DOWN : BlockFace.UP;
                Block currentBlock = banner;

                if (extending == true) {
                    int limit = 100;
                    Block nextBlock = currentBlock.getRelative(face);

                    while (limit-- > 0 && (nextBlock.getType() == org.bukkit.Material.AIR
                            || nextBlock.getType() == banner.getType())) {
                        if (columnBlocks.contains(currentBlock) == false)
                            columnBlocks.add(currentBlock);

                        currentBlock = nextBlock;
                        nextBlock = currentBlock.getRelative(face);
                    }
                } else {
                    if (face == BlockFace.UP) {
                        int limit = 100;
                        Block nextBlock = currentBlock.getRelative(face);

                        while (limit-- > 0 && nextBlock.getType() == banner.getType()) {
                            if (columnBlocks.contains(currentBlock) == false)
                                columnBlocks.add(currentBlock);
                            currentBlock = nextBlock;
                            nextBlock = currentBlock.getRelative(face);
                        }
                    } else {
                        int limit = 100;
                        currentBlock = currentBlock.getRelative(face);

                        while (limit-- > 0 && currentBlock.getType() == banner.getType()) {
                            if (columnBlocks.contains(currentBlock) == false)
                                columnBlocks.add(currentBlock);

                            currentBlock = currentBlock.getRelative(face);
                        }
                    }
                }
            }

            for (Block block : columnBlocks) {
                if (extending == true)
                    block.setBlockData(bannerData.clone(), true);
                else
                    block.setType(org.bukkit.Material.AIR);
            }
        }
    }
}