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
import com.atlassian.jira.util.json.JSONException;
import ru.mail.jira.plugins.up.common.Utils;
import ru.mail.jira.plugins.up.structures.ProjRole;

import java.util.*;


/**
 * Role group single custom field.
 * 
 * @author Andrey Markelov
 */
public class RoleGroupUserField extends UserCFType
{
    /**
     * Plugin data.
     */
    private final PluginData data;

    /**
     * Group manager.
     */
    private final GroupManager grMgr;

    /**
     * Project role manager.
     */
    private final ProjectRoleManager projectRoleManager;

    private final String baseUrl;

    /**
     * Constructor.
     */
    public RoleGroupUserField(
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
            UserFilterManager userFilterManager, UserBeanFactory userBeanFactory)
    {
        super(customFieldValuePersister, userConverter, genericConfigManager, applicationProperties,
                authenticationContext, fieldConfigSchemeManager, projectManager, soyTemplateRendererProvider,
                grMgr, projectRoleManager, searchService, jiraBaseUrls, userHistoryManager,
                userFilterManager, ComponentAccessor.getJiraAuthenticationContext().getI18nHelper(),
                userBeanFactory);
        this.data = data;
        this.grMgr = grMgr;
        this.projectRoleManager = projectRoleManager;
        this.baseUrl = appProp.getBaseUrl();
    }

    @Override
    public Map<String, Object> getVelocityParameters(Issue issue,
        CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> params = super.getVelocityParameters(issue, field,
            fieldLayoutItem);

        /* Load custom field parameters */

        List<String> groups = new ArrayList<String>();
        List<ProjRole> projRoles = new ArrayList<ProjRole>();
        try
        {
            Utils.fillDataLists(data.getRoleGroupFieldData(field.getId()),
                groups, projRoles);
        }
        catch (JSONException e)
        {
            log.error(
                "RoleGroupUserField::getVelocityParameters - Incorrect field data",
                e);
            // --> impossible
        }

        List<String> highlightedGroups = new ArrayList<String>();
        List<ProjRole> highlightedProjRoles = new ArrayList<ProjRole>();
        try
        {
            Utils.fillDataLists(
                data.getHighlightedRoleGroupFieldData(field.getId()),
                highlightedGroups, highlightedProjRoles);
        }
        catch (JSONException e)
        {
            log.error(
                "RoleGroupUserField::getVelocityParameters - Incorrect field data",
                e);
            // --> impossible
        }

        /* Build possible values list */

        SortedSet<ApplicationUser> possibleUsers = Utils.buildUsersList(grMgr,
            projectRoleManager, issue.getProjectObject(), groups, projRoles);
        Set<ApplicationUser> allUsers = new HashSet<ApplicationUser>(possibleUsers);
        SortedSet<ApplicationUser> highlightedUsers = Utils.buildUsersList(grMgr,
            projectRoleManager, issue.getProjectObject(), highlightedGroups,
            highlightedProjRoles);
        highlightedUsers.retainAll(possibleUsers);
        possibleUsers.removeAll(highlightedUsers);

        Map<String, String> highlightedUsersSorted = new LinkedHashMap<String, String>();
        Map<String, String> otherUsersSorted = new LinkedHashMap<String, String>();
        for (ApplicationUser user : highlightedUsers)
        {
            highlightedUsersSorted.put(user.getName(), user.getDisplayName());
        }
        for (ApplicationUser user : possibleUsers)
        {
            otherUsersSorted.put(user.getName(), user.getDisplayName());
        }

        params.put("allUsers", allUsers);
        params.put("isautocomplete", data.isAutocompleteView(field.getId()));
        params.put("baseUrl", baseUrl);
        params.put("highlightedUsersSorted", highlightedUsersSorted);
        params.put("otherUsersSorted", otherUsersSorted);

        Utils.addViewAndEditParameters(params, field.getId());

        return params;
    }
}
