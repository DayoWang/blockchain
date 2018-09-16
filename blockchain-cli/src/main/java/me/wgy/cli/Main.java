package me.wgy.cli;

/**
 * Cli 入口
 *
 * @author wgy
 * @date 2018/9/16
 */
public class Main {

  public static void main(String[] args) {
    CliService cli = new CliService(args);
    cli.parse();
  }
}
