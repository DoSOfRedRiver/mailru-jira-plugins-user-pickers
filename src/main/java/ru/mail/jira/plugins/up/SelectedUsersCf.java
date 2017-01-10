/*
 * Created by Andrey Markelov 11-11-2012. Copyright Mail.Ru Group 2012. All
 * rights reserved.
 */
package ru.mail.jira.plugins.up;


import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.impl.UserCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.UserBeanFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserUtil;
import ru.mail.jira.plugins.up.common.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Single selected users field.
 * 
 * @author Andrey Markelov
 */
public class SelectedUsersCf extends UserCFType
{
    /**
     * Plugin data.
     */
    private final PluginData data;

    /**
     * User util.
     */
    private final UserUtil userUtil;

    private final String baseUrl;

    /**
     * Constructor.
     */
    public SelectedUsersCf(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext,
            UserSearchService searchService, PluginData data,
            GroupManager grMgr, ProjectManager projectManager,
            UserConverter userConverter, com.atlassian.sal.api.ApplicationProperties appProp,
            FieldConfigSchemeManager fieldConfigSchemeManager,
            SoyTemplateRendererProvider soyTemplateRendererProvider, ProjectRoleManager projectRoleManager,
            JiraBaseUrls jiraBaseUrls, UserHistoryManager userHistoryManager,
            UserFilterManager userFilterManager, UserBeanFactory userBeanFactory, UserUtil userUtil)
    {
        super(customFieldValuePersister, userConverter, genericConfigManager, applicationProperties,
                authenticationContext, fieldConfigSchemeManager, projectManager, soyTemplateRendererProvider,
                grMgr, projectRoleManager, searchService, jiraBaseUrls, userHistoryManager,
                userFilterManager, ComponentAccessor.getJiraAuthenticationContext().getI18nHelper(),
                userBeanFactory);
        this.data = data;
        this.userUtil = userUtil;

        baseUrl = appProp.getBaseUrl();
    }

    @Override
    public Map<String, Object> getVelocityParameters(Issue issue,
        CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> params = super.getVelocityParameters(issue, field,
            fieldLayoutItem);

        Map<String, String> map = new HashMap<String, String>();
        Set<String> users = data.getStoredUsers(field.getId());
        for (String user : users)
        {
            ApplicationUser userObj = userUtil.getUserObject(user);
            if (userObj != null)
            {
                map.put(userObj.getName(), userObj.getDisplayName());
            }
        }

        TreeMap<String, String> sorted_map = new TreeMap<String, String>(
            new ValueComparator(map));
        sorted_map.putAll(map);
        params.put("map", sorted_map);
        params.put("isautocomplete", data.isAutocompleteView(field.getId()));
        params.put("baseUrl", baseUrl);

        Utils.addViewAndEditParameters(params, field.getId());

        return params;
    }
}
