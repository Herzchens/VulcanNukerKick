package me.herzchen.vulcannukerkick;

import me.frep.vulcan.api.event.VulcanFlagEvent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VulcanListener implements Listener {

    private final VulcanNukerKick plugin;

    public VulcanListener(VulcanNukerKick plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVulcanFlag(VulcanFlagEvent event) {
        Player player = event.getPlayer();
        String checkName = event.getCheck().getName();
        String checkType = String.valueOf(event.getCheck().getType());

        if (!checkName.equalsIgnoreCase("Bad Packets") || !checkType.equalsIgnoreCase("Type Nuker")) {
            return;
        }

        YamlConfiguration offenses = plugin.getOffensesConfig();
        String key = "offenses." + player.getUniqueId();
        int count = offenses.getInt(key, 0) + 1;
        offenses.set(key, count);
        plugin.saveOffenses();

        FileConfiguration cfg = plugin.getConfig();
        String infoFormat = cfg.getString("info-format", "{info}");
        List<Map<?, ?>> puns = cfg.getMapList("punishments");

        int idx = Math.min(count - 1, puns.size() - 1);
        Map<?, ?> punish = puns.get(idx);

        String type = ((String) punish.get("type")).toUpperCase(Locale.ROOT);
        long duration = ((Number) punish.get("duration")).longValue();
        String rawMsg = (String) punish.get("message");

        String info = infoFormat.replace("{info}", event.getInfo());
        String msg = rawMsg.replace("{info}", info);

        Bukkit.getLogger().info("[VulcanNukerKick] " + player.getName() +
                " vi phạm lần " + count + " (" + type + ")");

        if ("KICK".equals(type)) {
            player.kickPlayer(msg);
        } else if ("BAN".equals(type)) {
            Date until = duration > 0
                    ? new Date(System.currentTimeMillis() + duration * 1000)
                    : null;
            Bukkit.getBanList(BanList.Type.NAME)
                    .addBan(player.getName(), msg, until, "VulcanNukerKick");
            player.kickPlayer(msg);
        }
    }
}
