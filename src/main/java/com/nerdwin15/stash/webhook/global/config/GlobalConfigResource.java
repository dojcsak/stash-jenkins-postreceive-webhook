package com.nerdwin15.stash.webhook.global.config;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

@Path("/")
public class GlobalConfigResource {

    private final UserManager userManager;

    private final PluginSettingsFactory pluginSettingsFactory;

    private final TransactionTemplate transactionTemplate;

    public GlobalConfigResource(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        return Response.ok(transactionTemplate.execute(new TransactionCallback<GlobalConfig>() {

            @Override
            public GlobalConfig doInTransaction() {
                PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                GlobalConfig config = new GlobalConfig();
                config.setExecutablePath((String) settings.get(GlobalConfig.class.getName() + ".executablePath"));
                config.setConfigHome((String) settings.get(GlobalConfig.class.getName() + ".configHome"));
                return config;
            }
        })).build();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final GlobalConfig config, @Context final HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        transactionTemplate.execute(new TransactionCallback() {

            @Override
            public Object doInTransaction() {
                PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                pluginSettings.put(GlobalConfig.class.getName() + ".executablePath", config.getExecutablePath());
                pluginSettings.put(GlobalConfig.class.getName() + ".configHome", config.getConfigHome());

                return null;
            }
        });
        return Response.noContent().build();
    }
}
