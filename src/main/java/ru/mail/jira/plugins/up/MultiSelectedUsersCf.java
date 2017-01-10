/*
 * Created by Andrey Markelov 11-11-2012. Copyright Mail.Ru Group 2012. All
 * rights reserved.
 */
package ru.mail.jira.plugins.up;


import com.atlassian.jira.avatar.Avatar.Size;
import com.atlassian.jira.avatar.AvatarServiceImpl;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.MultiUserConverter;
import com.atlassian.jira.issue.customfields.impl.MultiUserCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.UserBeanFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.FieldVisibilityManager;
import ru.mail.jira.plugins.up.common.Utils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Multi selected users field.
 * 
 * @author Andrey Markelov
 */
public class MultiSelectedUsersCf extends MultiUserCFType
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

    private Map<String, String> usersAvatars;

    private final AvatarServiceImpl avatarService;

    /**
     * Constructor.
     */
    public MultiSelectedUsersCf(
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext,
            FieldVisibilityManager fieldVisibilityManager, PluginData data,
            UserUtil userUtil, com.atlassian.sal.api.ApplicationProperties appProp,
            UserSearchService searchService, MultiUserConverter multiUserConverter,
            JiraBaseUrls jiraBaseUrls, UserBeanFactory userBeanFactory)
    {
        super(customFieldValuePersister, genericConfigManager, multiUserConverter,
                applicationProperties, authenticationContext, searchService,
                fieldVisibilityManager, jiraBaseUrls, userBeanFactory);
        this.data = data;
        this.userUtil = userUtil;
        baseUrl = appProp.getBaseUrl();

        this.avatarService = ComponentAccessor.getComponentOfType(AvatarServiceImpl.class);
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

        Object issueValObj = issue.getCustomFieldValue(field);
        Set<String> issueVal = Utils.convertList(issueValObj);
        params.put("selectVal", Utils.convertSetToString(issueVal));

        TreeMap<String, String> sorted_map = new TreeMap<String, String>(
            new ValueComparator(map));
        sorted_map.putAll(map);
        params.put("map", sorted_map);
        params.put("issueVal", issueVal);
        params.put("isautocomplete", data.isAutocompleteView(field.getId()));
        params.put("baseUrl", baseUrl);

        usersAvatars = new HashMap<String, String>(users.size());
        for (String userName : users)
        {
            ApplicationUser user = ComponentAccessor.getUserUtil()
                .getUserObject(userName);
            if (user != null)
            {
                usersAvatars.put(user.getName(), getUserAvatarUrl(user));
            }
        }
        params.put("usersAvatars", usersAvatars);

        Utils.addViewAndEditParameters(params, field.getId());

        return params;
    }

    private String getUserAvatarUrl(ApplicationUser user)
    {
        URI uri = avatarService.getAvatarURL(user, user.getName(), Size.SMALL);

        return uri.toString();
    }
}
