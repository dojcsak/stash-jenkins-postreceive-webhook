package com.nerdwin15.stash.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.event.branch.BranchChangedEvent;
import com.atlassian.bitbucket.event.branch.BranchCreatedEvent;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.event.api.EventListener;
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
     *
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
     *
     * @param event The branch changed event
     */
    @EventListener
    public void onRefsChangedEvent(BranchChangedEvent event) {
        if (settingsService.getSettings(event.getRepository()) == null) {
            return;
        }

        LOGGER.debug("BranchChangedEvent: " + event.getRepository().getName());

        AutojobsRunner.run("/usr/local/bin/jenkins-makejobs-git", "/opt/jenkins-autojobs",
                event.getRepository().getProject().getKey(), event.getRepository().getName());

        if (event instanceof BranchCreatedEvent) {
            for (RefChange refCh : event.getRefChanges()) {
                // Get branch name from ref 'refs/heads/master'
                // NOTE - this method gets called for tag changes too
                // In that case, the 'branch' passed to Jenkins will
                // be "refs/tags/TAGNAME"
                // Leaving this as-is in case someone relies on that...
                String strRef = refCh.getRef().getId().replaceFirst("refs/heads/", "");
                String strSha1 = refCh.getToHash();

                String user = (event.getUser() != null) ? event.getUser().getName() : null;
                EventContext context = new EventContext(event, event.getRepository(), user);

                if (filterChain.shouldDeliverNotification(context)) {
                    notifier.notifyBackground(context.getRepository(), strRef, strSha1);
                }
            }
        }

    }

}
