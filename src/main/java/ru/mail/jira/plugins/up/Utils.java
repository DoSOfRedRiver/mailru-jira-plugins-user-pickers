/*
 * Created by Andrey Markelov 11-11-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.mail.jira.plugins.up;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

/**
 * This class contains utility methods.
 * 
 * @author Andrey Markelov
 */
public class Utils
{
    /**
     * Convert string list to java list.
     */
    public static Set<String> convertList(Object obj)
    {
        Set<String> set = new LinkedHashSet<String>();
        if (obj == null)
        {
            return set;
        }

        if (obj instanceof Collection<?>)
        {
            Collection<User> users = (Collection<User>)obj;
            for (User user : users)
            {
                set.add(user.getName());
            }
        }
        else
        {
            String str = removeBrackets(obj.toString());
            StringTokenizer st = new StringTokenizer(str, ",");
            while (st.hasMoreTokens())
            {
                set.add(st.nextToken().trim());
            }
        }

        return set;
    }

    public static void fillDataLists(
        String shares_data,
        List<String> groups,
        List<ProjRole> projRoles)
    throws JSONException
    {
        if (shares_data == null || shares_data.length() == 0)
        {
            return;
        }

        JSONArray jsonObj = new JSONArray(shares_data);
        for (int i = 0; i < jsonObj.length(); i++)
        {
            JSONObject obj = jsonObj.getJSONObject(i);
            String type = obj.getString("type");
            if (type.equals("G"))
            {
                groups.add(obj.getString("group"));
            }
            else
            {
                ProjRole pr = new ProjRole(obj.getString("proj"), obj.getString("role"));
                projRoles.add(pr);
            }
        }
    }

    /**
     * Get base URL from HTTP request.
     */
    public static String getBaseUrl(HttpServletRequest req)
    {
        return (req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath());
    }

    public static Map<String, String> getProjectRoleUsers(
        ProjectRoleManager projectRoleManager,
        String role,
        Project currProj)
    {
        Map<String, String> map = new HashMap<String, String>();

        if (role.equals(""))
        {
            Collection<ProjectRole> projRoles = projectRoleManager.getProjectRoles();
            for (ProjectRole pRole : projRoles)
            {
                ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(pRole, currProj);
                Set<User> users = projectRoleActors.getUsers();
                for (User objUser : users)
                {
                    map.put(objUser.getName(), objUser.getDisplayName());
                }
            }
        }
        else
        {
            ProjectRole projRole = projectRoleManager.getProjectRole(Long.valueOf(role));
            ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projRole, currProj);
            Set<User> users = projectRoleActors.getUsers();
            for (User objUser : users)
            {
                map.put(objUser.getName(), objUser.getDisplayName());
            }
        }

        return map;
    }

    /**
     * Remove brackets from string.
     */
    public static String removeBrackets(String str)
    {
        if (str == null || str.length() == 0)
        {
            return "";
        }

        if (str.startsWith("["))
        {
            str = str.substring(1);
        }

        if (str.endsWith("]"))
        {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static String setToStr(Set<String> set)
    {
        StringBuilder sb = new StringBuilder();
        if (set != null)
        {
            for (String s : set)
            {
                sb.append(s).append(",");
            }
        }

        return sb.toString();
    }

    /**
     * Private constructor.
     */
    private Utils() {}
}
