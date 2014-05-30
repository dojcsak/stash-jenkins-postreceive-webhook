package com.nerdwin15.stash.webhook;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jenkins autojobs runner. See https://pythonhosted.org/jenkins-autojobs/index.html
 * 
 * @author Dojcsák Sándor (dojcsak)
 */
public class AutojobsRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutojobsRunner.class);

  /**
   * Run the jenkins-autojobs tool with the specified parameters.
   * 
   * @param executable The jenkins-make-git executable path
   * @param config The jenkins-make-git yaml config
   * @param projectKey Stash project key
   * @param repoName Stash repository name
   */
  public static void run(String executable, String config, String projectKey, String repoName) {
    try {
      Process pr = Runtime.getRuntime().exec(executable + " " + config + File.separator + projectKey + File.separator + repoName + ".yaml");
      pr.waitFor();

      LOGGER.debug(readProcessStream(pr.getInputStream()));

      String error = readProcessStream(pr.getErrorStream());
      if (error.length() > 0) {
        LOGGER.error(error);
      }

      LOGGER.debug("Jenkins autojobs successfully finished");
    } catch (IOException e) {
      LOGGER.error("Jenkins autojobs failed: ", e);
    } catch (InterruptedException e) {
      LOGGER.error("Jenkins autojobs failed: ", e);
    }
  }

  private static String readProcessStream(InputStream inputStream) throws IOException {
    BufferedReader errorReader = null;
    try {
      errorReader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      StringBuffer error = new StringBuffer();
      while ((line = errorReader.readLine()) != null) {
        error.append(line);
      }

      return error.toString();
    } finally {
      if (errorReader != null) {
        errorReader.close();
      }
    }
  }
}
