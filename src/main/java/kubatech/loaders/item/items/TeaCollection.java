/*
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022  kuba6000
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package kubatech.loaders.item.items;

import java.util.LinkedList;
import java.util.List;
import kubatech.api.utils.ModUtils;
import kubatech.loaders.ItemLoader;
import kubatech.loaders.item.ItemProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.AchievementPage;

public class TeaCollection extends ItemProxy {
    protected static TeaPage teapage;
    protected static LinkedList<Achievement> achievements;
    protected Achievement achievement;
    private final String achievementname;

    public TeaCollection(String unlocalizedName) {
        super("teacollection." + unlocalizedName, "tea/" + unlocalizedName);
        achievementname = "teacollection." + unlocalizedName;
    }

    private static final int[][] achievement_poses = new int[][] {
        {0, 0},
        {2, 0},
        {3, 1},
        {4, 2},
        {4, 4},
        {3, 5},
        {2, 6},
        {0, 6},
        {-1, 5},
        {-2, 4},
        {-2, 2},
        {-1, 1},
        {1, 3}
    };

    @Override
    public void ItemInit(int index) {
        super.ItemInit(index);
        if (teapage == null) {
            teapage = new TeaPage();
            AchievementPage.registerAchievementPage(teapage);
            achievements = teapage.getAchievementsOriginal();
        }
        achievements.add(
                achievement = new Achievement(
                                achievementname,
                                achievementname,
                                achievement_poses[index][0],
                                achievement_poses[index][1],
                                new ItemStack(ItemLoader.kubaitems, 1, index),
                                null)
                        .registerStat());
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer entity, List<String> tooltipList, boolean showDebugInfo) {
        if (stack.stackTagCompound != null
                && stack.stackTagCompound.hasKey("TeaOwner")
                && !stack.stackTagCompound.getString("TeaOwner").equals(entity.getCommandSenderName())) {
            tooltipList.add(EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC
                    + StatCollector.translateToLocal("kubaitem.notyours"));
            return;
        }
        tooltipList.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("kubaitem.fromcollection"));
        tooltipList.add(EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + ""
                + EnumChatFormatting.UNDERLINE + StatCollector.translateToLocal("kubaitem.teacollection"));
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.drink;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
        if (p_77659_1_.stackTagCompound == null || !p_77659_1_.stackTagCompound.hasKey("TeaOwner")) return p_77659_1_;
        if (!p_77659_1_.stackTagCompound.getString("TeaOwner").equals(p_77659_3_.getCommandSenderName()))
            return p_77659_1_;
        p_77659_3_.setItemInUse(p_77659_1_, 32);
        return p_77659_1_;
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer entity) {
        if (world.isRemote) return stack;
        if (!(entity instanceof EntityPlayerMP)) return stack;
        if (stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("TeaOwner")) return stack;
        if (!stack.stackTagCompound.getString("TeaOwner").equals(entity.getCommandSenderName())) return stack;
        entity.addChatComponentMessage(new ChatComponentText(
                EnumChatFormatting.GREEN + StatCollector.translateToLocal("kubaitem.teacollection.mmm")));
        entity.triggerAchievement(achievement);
        return stack;
    }

    @Override
    public int getMaxItemUseDuration() {
        return 32;
    }

    @Override
    public String getDisplayName(ItemStack stack) {
        if (!ModUtils.isClientSided) return super.getDisplayName(stack);
        if (stack.stackTagCompound == null
                || (!stack.stackTagCompound.hasKey("TeaOwner")
                        || stack.stackTagCompound
                                .getString("TeaOwner")
                                .equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName())))
            return super.getDisplayName(stack);
        return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + "???????";
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrentItem) {
        if (world.isRemote) return;
        if (!(entity instanceof EntityPlayerMP)) return;
        NBTTagCompound tag = stack.stackTagCompound;
        if (tag == null) tag = stack.stackTagCompound = new NBTTagCompound();
        if (tag.hasKey("display")) tag.removeTag("display");
        if (tag.hasKey("TeaOwner")) return;
        tag.setString("TeaOwner", entity.getCommandSenderName());
    }

    private static class TeaPage extends AchievementPage {

        public TeaPage() {
            super("Tea");
        }

        final LinkedList<Achievement> unlockedAchievements = new LinkedList<>();

        @Override
        public List<Achievement> getAchievements() {
            if (!ModUtils.isClientSided) return super.getAchievements();

            if (new Throwable().getStackTrace()[1].getMethodName().equals("isAchievementInPages"))
                return super.getAchievements(); // 5HEAD FIX

            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            unlockedAchievements.clear();
            for (Achievement achievement : achievements)
                if (player.getStatFileWriter().hasAchievementUnlocked(achievement))
                    unlockedAchievements.add(achievement);
            return unlockedAchievements;
        }

        private LinkedList<Achievement> getAchievementsOriginal() {
            return (LinkedList<Achievement>) super.getAchievements();
        }
    }
}
