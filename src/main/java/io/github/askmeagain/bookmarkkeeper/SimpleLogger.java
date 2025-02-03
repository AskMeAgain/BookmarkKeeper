package io.github.askmeagain.bookmarkkeeper;

import com.intellij.openapi.diagnostic.Logger;

public class SimpleLogger {
  private final Logger logger;
  private final String tag;

  public SimpleLogger(String tag) {
    this.logger = Logger.getInstance(tag);
    this.tag = "[KIR_LOG][" + tag + "] ";
  }

  public void info(String message) {
    logger.info(tag + message);
  }

  public void debug(String message) {
    logger.debug(tag + message);
  }

  public void warn(String message) {
    logger.warn(tag + message);
  }

  public void error(String message) {
    logger.error(tag + message);
  }

  public void error(String message, Throwable throwable) {
    logger.error(tag + message, throwable);
  }
}