package com.github.intangir.ConsoleScheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class ConsoleScheduler extends JavaPlugin
{
  static Logger log = Logger.getLogger("Minecraft");
  Date date = new Date();
  Calendar calendar = GregorianCalendar.getInstance();

  public void onEnable()
  {
    getConfig().options().copyDefaults(true);
    getConfig().options().header(
      "###############################################################\n# Hello! Thank you for using ConsoleScheduler. Below you find #\n# configuration settings for this plugin. Make sure you don't #\n# use TABs in your text, only spaces!                         #\n#                                                             #\n# If you have any questions or requests, don't be afraid to   #\n# send me a PM on minecraftforum.net! (username is Rahazan)   #\n# And ehm, consider donating if you like this plugin :x       #\n#                                                             #\n# Yours truly,                                                #\n# Rahazan                                                     #\n###############################################################\n# Configuration explanation below ~                           #\n  ALL TIMES ARE IN SECONDS!\nInitial delay is the time before the plugin starts starting \nthe commands in the schedule. This is in place so that other\nplugins have the time to start. You could set this to 0, \nbut errors may occur.\nMake sure the Command1, Command2, Command3 etc. are numbered\nin succession. This will ensure they all load.\n\nAlso make sure you enter a command, heh.\n\n1 minute = 60 seconds. 1 hour = 3600 seconds\nHOUR in 24-hour format!\nSpecificTime commands ALWAYS repeat!\n###############################################################\n");

    saveConfig();
    getConfig();
    PluginDescriptionFile pdfFile = getDescription();
    log.info("[" + pdfFile.getName() + "] (By Rahazan) - v" + pdfFile.getVersion() + " loaded.");
    log.info("[" + pdfFile.getName() + "] Command execution will start in " + getConfig().getInt("InitialDelay") + " seconds.");
    initialDelay();
  }

  public void onDisable()
  {
    PluginDescriptionFile pdfFile = getDescription();
    log.info("[" + pdfFile.getName() + "] (Rahazan) - v" + pdfFile.getVersion() + " shutdown.");
    getServer().getScheduler().cancelTasks(this);
  }

  public void initialDelay()
  {
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        ConsoleScheduler.log.info("[ConsoleScheduler] has started executing commands");
        ConsoleScheduler.log.info("---------------[ConsoleScheduler]----------------");
        ConsoleScheduler.this.startSchedule();
      }
    }
    , getConfig().getInt("InitialDelay") * 20L);
  }

  public void startSchedule() {
    int counter = 1; int started = 0;
    while (getConfig().contains("CommandSchedule.Command" + counter)) {
      log.info("getConfig contains CommandSchedule.Command" + counter);
      if ((!getConfig().contains("CommandSchedule.Command" + counter + ".After")) && 
        (!getConfig().getBoolean("CommandSchedule.Command" + counter + ".SpecificTime", false))) {
        log.info("[CommandScheduler] Command" + counter + " does not have an After value, defaulting to 0.");
        getConfig().set("CommandSchedule.Command" + counter + ".After", Integer.valueOf(0));
      }

      if (getConfig().getBoolean("CommandSchedule.Command" + counter + ".SpecificTime", false)) {
        timeTask(counter);
      }
      else if (getConfig().getBoolean("CommandSchedule.Command" + counter + ".Repeat")) {
        if (!getConfig().contains("CommandSchedule.Command" + counter + ".Interval")) {
          log.info("[ConsoleScheduler] Command" + counter + " has Repeat: true, but Interval is not set! Ignoring this command.");
        }
        else {
          repeatingTask(counter);
        }
      }
      else {
        nonrepeatingTask(counter);
      }
      started++;
      counter++;
    }

    log.info("[ConsoleScheduler] has attempted to put " + started + " commands on schedule.");
  }

  public void repeatingTask(final int counter)
  {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        ConsoleScheduler.this.runCommand(counter);
      }
    }
    , getConfig().getInt("CommandSchedule.Command" + counter + ".After", 0) * 20L, getConfig().getInt("CommandSchedule.Command" + counter + ".Interval") * 20L);
  }

  public void nonrepeatingTask(final int counter)
  {
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
    {
      public void run()
      {
        ConsoleScheduler.this.runCommand(counter);
      }
    }
    , getConfig().getInt("CommandSchedule.Command" + counter + ".After", 0) * 20L);
  }

  public void timeTask(final int counter) {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        ConsoleScheduler.this.runCommand(counter);
      }
    }
    , getOffset(counter) * 20L, 1728000L);
  }

  public void runCommand(int counter) {
    getServer().dispatchCommand(getServer().getConsoleSender(), getConfig().getString("CommandSchedule.Command" + counter + ".Command"));
  }

  public int getOffset(int counter)
  {
    this.calendar.setTime(this.date);

    int time_in_seconds = this.calendar.get(11) * 3600 + this.calendar.get(12) * 60 + this.calendar.get(13);
    int time_wanted = getConfig().getInt("CommandSchedule.Command" + counter + ".Hour", 0) * 3600 + getConfig().getInt("CommandSchedule.Command" + counter + ".Minute", 0) * 60 + getConfig().getInt("CommandSchedule.Command" + counter + ".Second", 0);
    int Offset;
    if (time_wanted >= time_in_seconds) {
      Offset = time_wanted - time_in_seconds;
    }
    else {
      Offset = 86400 + time_wanted - time_in_seconds;
    }
    return Offset;
  }
}