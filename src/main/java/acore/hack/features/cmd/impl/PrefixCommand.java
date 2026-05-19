package acore.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import acore.hack.core.Managers;
import acore.hack.features.cmd.Command;
import acore.hack.features.modules.misc.ClientSettings;

public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("prefix", "Change the command prefix.");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(literal("set").then(arg("prefix", StringArgumentType.greedyString()).executes(context -> {
         String prefix = (String)context.getArgument("prefix", String.class);
         Managers.COMMAND.setPrefix(prefix);
         sendMessage(Formatting.GREEN + "Changed prefix to " + prefix);
         ClientSettings.prefix.setValue(prefix);
         return 1;
      })));
      builder.executes(context -> {
         sendMessage(Formatting.GREEN + "Current prefix: " + Managers.COMMAND.getPrefix());
         return 1;
      });
   }
}
