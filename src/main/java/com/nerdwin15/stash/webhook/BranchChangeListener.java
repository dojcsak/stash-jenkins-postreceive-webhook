package com.nerdwin15.stash.webhook;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.event.api.EventListener;
import com.atlassian.stash.branch.BranchChangedEvent;
import com.atlassian.stash.branch.BranchCreatedEvent;
import com.atlassian.stash.branch.BranchDeletedEvent;
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

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param settingsService Service to be used to get the Settings
   */
  public BranchChangeListener(EligibilityFilterChain filterChain,
      Notifier notifier, SettingsService settingsService) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.settingsService = settingsService;
  }

  /**
   * Event listener that is notified of both branch created and deleted events
   * @param event The branch changed event
   */
  @EventListener
  public void onRefsChangedEvent(BranchChangedEvent event) {
    if (settingsService.getSettings(event.getRepository()) == null) {
      // TODO: jenkins-autojobs is enabled?
      return;
    }

    LOGGER.debug("BranchChangedEvent: " + event.getRepository().getName());

    AutojobsRunner.run("/usr/local/bin/jenkins-makejobs-git", "/opt/jenkins-autojobs", event.getRepository().getProject().getKey(), event.getRepository()
        .getName());

    if (event instanceof BranchCreatedEvent) {
      String user = (event.getUser() != null) ? event.getUser().getName() : null;
      EventContext context = new EventContext(event, event.getRepository(), user);
      
      if (filterChain.shouldDeliverNotification(context)) {
        notifier.notifyBackground(context.getRepository());
      }
    }
    
  }

}
