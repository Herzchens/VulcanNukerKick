package me.herzchen.vulcannukerkick;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class VulcanNukerKick extends JavaPlugin {

    private File offensesFile;
    private YamlConfiguration offensesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        offensesFile = new File(getDataFolder(), "offenses.yml");
        if (!offensesFile.exists()) {
            saveResource("offenses.yml", false);
        }
        offensesConfig = YamlConfiguration.loadConfiguration(offensesFile);
        checkAndResetOffenses();

        getServer().getPluginManager().registerEvents(new VulcanListener(this), this);
        getLogger().info("VulcanNukerKick đã được kích hoạt!");
    }

    @Override
    public void onDisable() {
        saveOffenses();
    }

    public void reloadPluginConfig() {
        reloadConfig();
        offensesConfig = YamlConfiguration.loadConfiguration(offensesFile);
        checkAndResetOffenses();
    }

    public YamlConfiguration getOffensesConfig() {
        return offensesConfig;
    }

    public void saveOffenses() {
        try {
            offensesConfig.save(offensesFile);
        } catch (IOException e) {
            getLogger().severe("Không thể lưu offenses.yml: " + e.getMessage());
        }
    }

    private void checkAndResetOffenses() {
        String lastResetStr = offensesConfig.getString("lastReset", "");
        LocalDate lastReset = lastResetStr.isEmpty() ? LocalDate.now().minusDays(999) : LocalDate.parse(lastResetStr);
        int resetDays = getConfig().getInt("reset-days", 14);

        if (LocalDate.now().isAfter(lastReset.plusDays(resetDays))) {
            offensesConfig.set("offenses", null);
            offensesConfig.set("lastReset", LocalDate.now().toString());
            saveOffenses();
            getLogger().info("Reset offenses sau " + resetDays + " ngày.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("vkreload") && sender.hasPermission("vulcankicker.reload")) {
            reloadPluginConfig();
            sender.sendMessage("§aConfig và offenses đã được reload!");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("vkstats") && args.length == 1) {
            Player target = getServer().getPlayerExact(args[0]);
            String uuid = target != null ? target.getUniqueId().toString() : args[0];
            int count = offensesConfig.getInt("offenses." + uuid, 0);
            sender.sendMessage("§e" + args[0] + " đã vi phạm §c" + count + "§e lần.");
            return true;
        }
        return false;
    }
}
