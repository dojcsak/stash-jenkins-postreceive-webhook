package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.branch.BranchChangedEvent;
import com.atlassian.stash.branch.BranchDeletedEvent;
import com.atlassian.stash.event.RepositoryPushEvent;
import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;
import com.nerdwin15.stash.webhook.service.eligibility.EventContext;

/**
 * Listener for repository change events.  
 * 
 * Since it hears {@link RepositoryRefsChangedEvent} implementations, it is 
 * notified upon {@link RepositoryPushEvent} and {@link PullRequestMergedEvent}
 * events.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class RepositoryChangeListener {

  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;
  private final SettingsService settingsService;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param settingsService Service to be used to get the Settings
   */
  public RepositoryChangeListener(EligibilityFilterChain filterChain,
      Notifier notifier, SettingsService settingsService) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.settingsService = settingsService;
  }

  /**
   * Event listener that is notified of both pull request merges and push events
   * @param event The pull request event
   */
  @EventListener
  public void onRefsChangedEvent(RepositoryRefsChangedEvent event) {
    if (settingsService.getSettings(event.getRepository()) == null) {
      return;
    }
    
    if (event instanceof BranchChangedEvent || event instanceof BranchDeletedEvent) {
        return;
    }
    
    String user = (event.getUser() != null) ? event.getUser().getName() : null;
    EventContext context = new EventContext(event, event.getRepository(), user);
    
    if (filterChain.shouldDeliverNotification(context)) {
      notifier.notifyBackground(context.getRepository());
    }
  }

}
