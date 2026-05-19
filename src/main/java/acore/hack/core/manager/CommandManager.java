package acore.hack.core.manager;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import acore.hack.core.manager.IManager;
import acore.hack.features.cmd.Command;
import acore.hack.features.cmd.impl.BindCommand;
import acore.hack.features.cmd.impl.BlockESPCommand;
import acore.hack.features.cmd.impl.CfgCommand;
import acore.hack.features.cmd.impl.DropAllCommand;
import acore.hack.features.cmd.impl.EClipCommand;
import acore.hack.features.cmd.impl.FriendCommand;
import acore.hack.features.cmd.impl.GpsCommand;
import acore.hack.features.cmd.impl.HClipCommand;
import acore.hack.features.cmd.impl.HelpCommand;
import acore.hack.features.cmd.impl.MacroCommand;
import acore.hack.features.cmd.impl.ModuleCommand;
import acore.hack.features.cmd.impl.PrefixCommand;
import acore.hack.features.cmd.impl.StaffCommand;
import acore.hack.features.cmd.impl.VClipCommand;

public class CommandManager implements IManager {
   private String prefix = ".";
   private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher();
   private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
   private final List<Command> commands = new ArrayList<>();

   public CommandManager() {
      this.add(new GpsCommand());
      this.add(new CfgCommand());
      this.add(new BindCommand());
      this.add(new HelpCommand());
      this.add(new EClipCommand());
      this.add(new HClipCommand());
      this.add(new MacroCommand());
      this.add(new StaffCommand());
      this.add(new VClipCommand());
      this.add(new FriendCommand());
      this.add(new ModuleCommand());
      this.add(new PrefixCommand());
      this.add(new DropAllCommand());
      this.add(new BlockESPCommand());
   }

   private void add(@NotNull Command command) {
      command.register(this.dispatcher);
      this.commands.add(command);
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public Command get(Class<? extends Command> commandClass) {
      for (Command command : this.commands) {
         if (command.getClass().equals(commandClass)) {
            return command;
         }
      }
      return null;
   }

   @NotNull
   public static String getClientMessage() {
      return Formatting.WHITE + "⌊" + Formatting.GOLD + "⚡" + Formatting.WHITE + "⌉" + Formatting.RESET;
   }

   public List<Command> getCommands() {
      return this.commands;
   }

   public CommandSource getSource() {
      return this.source;
   }

   public CommandDispatcher<CommandSource> getDispatcher() {
      return this.dispatcher;
   }

   public void registerCommand(Command command) {
      if (command != null) {
         command.register(this.dispatcher);
         this.commands.add(command);
      }
   }
  }
