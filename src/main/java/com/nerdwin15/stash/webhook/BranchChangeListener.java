package com.nerdwin15.stash.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.event.api.EventListener;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.branch.BranchChangedEvent;
import com.atlassian.stash.branch.BranchCreatedEvent;
import com.nerdwin15.stash.webhook.global.config.GlobalConfigResource;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;
import com.nerdwin15.stash.webhook.service.eligibility.EventContext;

/**
 * Listener for repository branch change events.
 * 
 * @author Dojcsák Sándor (dojcsak)
 */
public class BranchChangeListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(BranchChangeListener.class);
  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;
  private final SettingsService settingsService;
  private final PluginSettingsFactory pluginSettingsFactory;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param settingsService Service to be used to get the Settings
   * @param pluginSettingsFactory Factory to be used to get the global settings of the plugin.
   */
  public BranchChangeListener(EligibilityFilterChain filterChain, Notifier notifier,
      SettingsService settingsService, PluginSettingsFactory pluginSettingsFactory) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.settingsService = settingsService;
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  /**
   * Event listener that is notified of both branch created and deleted events
   * @param event The branch changed event
   */
  @EventListener
  public void onRefsChangedEvent(BranchChangedEvent event) {
    if (settingsService.getSettings(event.getRepository()) == null
        || !settingsService.getSettings(event.getRepository()).getBoolean(Notifier.AUTOJOBS_ENABLED, false)) {
      return;
    }

    LOGGER.debug("BranchChangedEvent: " + event.getRepository().getName());

    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

    AutojobsRunner.run((String) pluginSettings.get(GlobalConfigResource.EXECUTABLE_PATH), (String) pluginSettings.get(GlobalConfigResource.CONFIG_HOME),
        event.getRepository().getProject().getKey(), event.getRepository().getName());
  
    if (event instanceof BranchCreatedEvent) {
      String user = (event.getUser() != null) ? event.getUser().getName() : null;
      EventContext context = new EventContext(event, event.getRepository(), user);
  
      if (filterChain.shouldDeliverNotification(context)) {
        notifier.notifyBackground(context.getRepository());
      }
    }    
  }  
}
