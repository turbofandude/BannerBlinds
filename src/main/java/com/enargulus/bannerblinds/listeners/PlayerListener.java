package com.enargulus.bannerblinds.listeners;

import com.enargulus.bannerblinds.BannerBlinds;
import org.bukkit.event.block.Action;
import org.bukkit.event.Listener;

import java.util.ArrayList;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.event.Event.Result;

public class PlayerListener implements Listener {

    private final BannerBlinds plugin;

    public PlayerListener(BannerBlinds plugin) {
        this.plugin = plugin;
    }

    private static boolean IsBanner(Block block) {
        switch (block.getType()) {
            case WHITE_WALL_BANNER:
            case ORANGE_WALL_BANNER:
            case MAGENTA_WALL_BANNER:
            case LIGHT_BLUE_WALL_BANNER:
            case YELLOW_WALL_BANNER:
            case LIME_WALL_BANNER:
            case PINK_WALL_BANNER:
            case GRAY_WALL_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case CYAN_WALL_BANNER:
            case PURPLE_WALL_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_WALL_BANNER:
            case GREEN_WALL_BANNER:
            case RED_WALL_BANNER:
            case BLACK_WALL_BANNER:
                return true;
            default:
                return false;
        }
    }

    private static boolean IsBlockBehindValid(Block block) {
        if (block == null || IsBanner(block) == true || block.getType().isSolid() == false)
            return false;
        return true;
    }

    private static boolean FacingSameDirection(Block block1, Block block2) {
        BlockData blockData1 = block1.getBlockData();
        BlockData blockData2 = block2.getBlockData();

        if (blockData1 instanceof org.bukkit.block.data.Directional directionalData1
                && blockData2 instanceof org.bukkit.block.data.Directional directionalData2) {
            return directionalData1.getFacing() == directionalData2.getFacing();
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Handle player join event
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isSneaking() == true)
            return; // ignore off-hand

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || PlayerListener.IsBanner(clickedBlock) == false)
            return;

        BlockData _blockData = clickedBlock.getBlockData();
        BlockFace facing = null;

        if (_blockData instanceof org.bukkit.block.data.Directional directionalData)
            facing = directionalData.getFacing();
        else
            return;

        event.setUseInteractedBlock(Result.ALLOW);
        event.setUseItemInHand(Result.DENY);

        if (event.getHand() != EquipmentSlot.HAND)
            return;

         if (!event.getPlayer().hasPermission("bannerblinds.use")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You don't have permission.");
            return;
        }

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

                if (PlayerListener.FacingSameDirection(clickedBlock, newBlock) == false) {
                    break;
                }

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

                    while (limit-- > 0 && IsBlockBehindValid(currentBlock.getRelative(facing.getOppositeFace()))
                            && (nextBlock.getType() == org.bukkit.Material.AIR
                                    || (nextBlock.getType() == banner.getType())
                                            && PlayerListener.FacingSameDirection(banner, nextBlock) == true)) {
                        if (columnBlocks.contains(currentBlock) == false)
                            columnBlocks.add(currentBlock);

                        currentBlock = nextBlock;
                        nextBlock = currentBlock.getRelative(face);
                    }
                } else {
                    if (face == BlockFace.UP) {
                        int limit = 100;
                        Block nextBlock = currentBlock.getRelative(face);

                        while (limit-- > 0 && nextBlock.getType() == banner.getType()
                                && PlayerListener.FacingSameDirection(banner, nextBlock) == true) {
                            if (columnBlocks.contains(currentBlock) == false)
                                columnBlocks.add(currentBlock);
                            currentBlock = nextBlock;
                            nextBlock = currentBlock.getRelative(face);
                        }
                    } else {
                        int limit = 100;
                        currentBlock = currentBlock.getRelative(face);

                        while (limit-- > 0 && currentBlock.getType() == banner.getType()
                                && PlayerListener.FacingSameDirection(banner, currentBlock) == true) {
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

        clickedBlock.getWorld().playSound(
            clickedBlock.getLocation(),
            extending ? Sound.BLOCK_WOOL_PLACE : Sound.BLOCK_WOOL_BREAK,
            SoundCategory.BLOCKS,
            1.0f,
            1.5f
        );
    }
}