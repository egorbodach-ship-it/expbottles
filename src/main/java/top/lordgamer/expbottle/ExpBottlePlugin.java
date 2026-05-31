package top.lordgamer.expbottle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ExpBottle - кастомный бутылёк опыта.
 * Каждый бутылёк хранит точное число уровней в PersistentDataContainer.
 * ПКМ (кинуть под себя) -> игрок получает ровно эти уровни.
 */
public class ExpBottlePlugin extends JavaPlugin implements Listener, CommandExecutor {

    private NamespacedKey levelsKey;

    @Override
    public void onEnable() {
        this.levelsKey = new NamespacedKey(this, "exp_levels");
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("expbottle") != null) {
            getCommand("expbottle").setExecutor(this);
        }
        getLogger().info("ExpBottle включён.");
    }

    /** Создаёт кастомный бутылёк опыта на N уровней. */
    private ItemStack createBottle(int levels, int amount) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color("&a&lБутылёк опыта &f" + levels + " lvl"));
            List<String> lore = new ArrayList<>();
            lore.add(color("&7"));
            lore.add(color("&a⊞ &7Кинь под себя (ПКМ), чтобы получить"));
            lore.add(color("&a⊞ &f" + levels + " &7уровней опыта."));
            lore.add(color("&7"));
            lore.add(color("&8» Особый предмет ExpBottle"));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(levelsKey, PersistentDataType.INTEGER, levels);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.EXPERIENCE_BOTTLE) return;
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) return;
        Integer levels = meta.getPersistentDataContainer().get(levelsKey, PersistentDataType.INTEGER);
        if (levels == null) return; // не наш бутылёк -> ванильное поведение

        event.setCancelled(true); // отменяем ванильный бросок

        hand.setAmount(hand.getAmount() - 1); // съедаем один
        player.giveExpLevels(levels); // выдаём ровно N уровней

        Location loc = player.getLocation();
        World w = player.getWorld();
        w.spawnParticle(Particle.SPELL_INSTANT, loc.clone().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.1);
        w.spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.1);
        w.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        w.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.sendMessage(color("&a✔ &7Получено &f" + levels + " &7уровней опыта!"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("expbottle.give")) {
                sender.sendMessage(color("&cНет прав."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(color("&c/expbottle give <игрок> <уровни> [кол-во]"));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(color("&cИгрок не найден: " + args[1]));
                return true;
            }
            int levels;
            try {
                levels = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(color("&cУровни должны быть числом."));
                return true;
            }
            if (levels <= 0) {
                sender.sendMessage(color("&cУровни должны быть > 0."));
                return true;
            }
            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Math.max(1, Integer.parseInt(args[3]));
                } catch (NumberFormatException ignored) {
                }
            }
            ItemStack bottle = createBottle(levels, amount);
            Map<Integer, ItemStack> leftover = target.getInventory().addItem(bottle);
            for (ItemStack rest : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), rest);
            }
            sender.sendMessage(color("&aВыдано " + amount + " x бутылёк опыта (" + levels + " lvl) игроку " + target.getName()));
            return true;
        }
        sender.sendMessage(color("&7ExpBottle: &f/expbottle give <игрок> <уровни> [кол-во]"));
        return true;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
